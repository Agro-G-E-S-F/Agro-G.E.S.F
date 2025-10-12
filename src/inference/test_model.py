#!/usr/bin/env python3
"""
Script para analisar a performance do modelo e entender onde está confundindo
"""

import torch
import torch.nn.functional as F
from PIL import Image
import os
import numpy as np
from collections import defaultdict, Counter
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.metrics import confusion_matrix, classification_report
import pandas as pd

from teste_image_cpu import PlantDiseasePredictor


class ModelAnalyzer:
    def __init__(self, predictor, val_data_path):
        self.predictor = predictor
        self.val_data_path = val_data_path
        self.results = []
        self.predictions = []
        self.true_labels = []
        
    def analyze_validation_set(self):
        """Analisa todo o conjunto de validação"""
        print("🔍 Analisando conjunto de validação...")
        
        class_stats = defaultdict(lambda: {'correct': 0, 'total': 0, 'predictions': []})
        
        for class_idx, class_name in enumerate(self.predictor.classes):
            class_path = os.path.join(self.val_data_path, class_name)
            
            if not os.path.exists(class_path):
                print(f"⚠️  Diretório não encontrado: {class_path}")
                continue
            
            print(f"Analisando classe: {class_name}")
            
            # Pegar algumas imagens da classe
            image_files = [f for f in os.listdir(class_path) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
            
            # Limitar a análise para não demorar muito (pegar 20 imagens por classe)
            sample_size = min(20, len(image_files))
            image_files = image_files[:sample_size]
            
            for img_file in image_files:
                img_path = os.path.join(class_path, img_file)
                
                try:
                    result = self.predictor.predict_single_image(img_path)
                    
                    if result:
                        predicted_class = result['predicted_class']
                        confidence = result['confidence']
                        predicted_idx = result['predicted_idx']
                        
                        # Registrar resultado
                        is_correct = predicted_class == class_name
                        class_stats[class_name]['total'] += 1
                        if is_correct:
                            class_stats[class_name]['correct'] += 1
                        
                        class_stats[class_name]['predictions'].append({
                            'image': img_file,
                            'predicted': predicted_class,
                            'confidence': confidence,
                            'correct': is_correct
                        })
                        
                        # Para matriz de confusão
                        self.true_labels.append(class_idx)
                        self.predictions.append(predicted_idx)
                        
                        # Mostrar erro se confiança alta mas errado
                        if not is_correct and confidence > 0.7:
                            print(f"  ❌ {img_file}: {class_name} → {predicted_class} ({confidence:.3f})")
                        elif is_correct and confidence > 0.8:
                            print(f"  ✅ {img_file}: {confidence:.3f}")
                            
                except Exception as e:
                    print(f"  ⚠️  Erro processando {img_file}: {e}")
        
        return class_stats
    
    def print_analysis_report(self, class_stats):
        """Imprime relatório detalhado da análise"""
        print("\n" + "="*60)
        print("📊 RELATÓRIO DE ANÁLISE DO MODELO")
        print("="*60)
        
        total_correct = 0
        total_images = 0
        
        print(f"{'Classe':<20} {'Accuracy':<10} {'Total':<8} {'Erros Principais'}")
        print("-" * 60)
        
        confusion_data = defaultdict(Counter)
        
        for class_name, stats in class_stats.items():
            if stats['total'] > 0:
                accuracy = stats['correct'] / stats['total']
                total_correct += stats['correct']
                total_images += stats['total']
                
                # Contar principais confusões
                wrong_predictions = [p['predicted'] for p in stats['predictions'] if not p['correct']]
                main_confusions = Counter(wrong_predictions).most_common(2)
                confusion_str = ', '.join([f"{cls}({cnt})" for cls, cnt in main_confusions])
                
                print(f"{class_name:<20} {accuracy:<10.3f} {stats['total']:<8} {confusion_str}")
                
                # Para análise mais detalhada
                for pred_info in stats['predictions']:
                    if not pred_info['correct']:
                        confusion_data[class_name][pred_info['predicted']] += 1
        
        overall_accuracy = total_correct / total_images if total_images > 0 else 0
        print("-" * 60)
        print(f"{'OVERALL':<20} {overall_accuracy:<10.3f} {total_images:<8}")
        
        # Principais problemas
        print(f"\n🎯 PRINCIPAIS CONFUSÕES:")
        for true_class, pred_counter in confusion_data.items():
            if pred_counter:
                main_confusion = pred_counter.most_common(1)[0]
                print(f"  {true_class} → {main_confusion[0]} ({main_confusion[1]} vezes)")
    
    def analyze_confidence_distribution(self, class_stats):
        """Analisa a distribuição de confiança das predições"""
        print(f"\n📈 ANÁLISE DE CONFIANÇA:")
        
        correct_confidences = []
        incorrect_confidences = []
        
        for class_name, stats in class_stats.items():
            for pred in stats['predictions']:
                if pred['correct']:
                    correct_confidences.append(pred['confidence'])
                else:
                    incorrect_confidences.append(pred['confidence'])
        
        if correct_confidences:
            print(f"Confiança média (corretas): {np.mean(correct_confidences):.3f}")
            print(f"Confiança mediana (corretas): {np.median(correct_confidences):.3f}")
        
        if incorrect_confidences:
            print(f"Confiança média (incorretas): {np.mean(incorrect_confidences):.3f}")
            print(f"Confiança mediana (incorretas): {np.median(incorrect_confidences):.3f}")
        
        # Verificar predições incorretas com alta confiança (problema sério)
        high_conf_wrong = [c for c in incorrect_confidences if c > 0.8]
        if high_conf_wrong:
            print(f"⚠️  Predições incorretas com alta confiança (>0.8): {len(high_conf_wrong)}")
            print(f"   Isso indica overfitting ou dados problemáticos!")
    
    def suggest_improvements(self, class_stats):
        """Sugere melhorias baseadas na análise"""
        print(f"\n💡 SUGESTÕES DE MELHORIA:")
        
        # Classes com baixa performance
        low_performance = []
        for class_name, stats in class_stats.items():
            if stats['total'] > 0:
                accuracy = stats['correct'] / stats['total']
                if accuracy < 0.7:  # Menos de 70%
                    low_performance.append((class_name, accuracy))
        
        if low_performance:
            print(f"📉 Classes com baixa performance (<70%):")
            for class_name, acc in low_performance:
                print(f"   - {class_name}: {acc:.1%}")
            print(f"   → Revisar qualidade das imagens dessas classes")
            print(f"   → Considerar mais data augmentation")
            print(f"   → Verificar se há overlabeling ou mislabeling")
        
        # Análise geral
        total_accuracy = sum(s['correct'] for s in class_stats.values()) / sum(s['total'] for s in class_stats.values())
        
        if total_accuracy < 0.8:
            print(f"⚠️  Accuracy geral baixa ({total_accuracy:.1%}). Considerações:")
            print(f"   1. Aumentar épocas de treinamento")
            print(f"   2. Ajustar learning rate")
            print(f"   3. Usar data augmentation mais agressivo")
            print(f"   4. Considerar transfer learning (pré-trained model)")
            print(f"   5. Revisar qualidade e diversidade dos dados")


def main():
    # Caminhos - ajustar conforme necessário
    model_path = "../outputs/modelo_final.pth"
    val_data_path = "../../data/val"  # Ajustar para seu caminho
    
    try:
        # Importar e inicializar preditor
        # Assumindo que você tem o código do preditor disponível
        print("Carregando modelo...")
        predictor = PlantDiseasePredictor(model_path, device='cpu')
        
        print("⚠️  Para executar esta análise:")
        print("1. Certifique-se que o preditor está funcionando")
        print("2. Ajuste os caminhos do modelo e dados de validação")
        print("3. Execute a análise completa")
        
        print(f"\n🔍 INVESTIGAÇÕES RECOMENDADAS:")
        print(f"1. Verificar se há imbalance real nos dados")
        print(f"2. Analisar qualidade visual das imagens")
        print(f"3. Verificar se preprocessing está correto")
        print(f"4. Revisar arquitetura vs complexidade do problema")
        print(f"5. Analisar curvas de loss durante treinamento")
        
        # Se tiver o preditor funcionando, descomente:
        analyzer = ModelAnalyzer(predictor, val_data_path)
        class_stats = analyzer.analyze_validation_set()
        analyzer.print_analysis_report(class_stats)
        analyzer.analyze_confidence_distribution(class_stats)
        analyzer.suggest_improvements(class_stats)
        
        print("Classes no modelo:")
        for i, classe in enumerate(predictor.classes):
            print(f"{i}: {classe}")
    except Exception as e:
        print(f"Erro: {e}")
        print("Certifique-se que todos os caminhos estão corretos")


if __name__ == "__main__":
    main()