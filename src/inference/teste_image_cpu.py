import torch
import torch.nn as nn
import torchvision.transforms as transforms
from torchvision import models
from PIL import Image
import torch.nn.functional as F


class PlantDiseasePredictor:
    def __init__(self, model_path, device='cpu'):
        self.device = torch.device(device)
        # Classes padrão (serão atualizadas se encontradas no checkpoint)
        self.classes = [
            'Acaro',           # 0
            'Diabrotica',      # 1
            'Doenca_Bacteriana', # 2
            'Doencas_Virais',  # 3
            'Ferrugem',        # 4
            'Lagarta',         # 5
            'Pinta_Preta',     # 6
            'Requeima',        # 7
            'Saudavel'         # 8
        ]
        
        # Transform padrão para imagens
        self.transform = transforms.Compose([
            transforms.Resize((224, 224)),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], 
                               std=[0.229, 0.224, 0.225])
        ])
        
        print(f"Inicializando preditor com device: {self.device}")
        
        # Carregar modelo
        self.model = self._load_model(model_path)
        
        if self.model is None:
            raise Exception("Falha ao carregar o modelo")
        
        print(f"Total de classes: {len(self.classes)}")

    def _load_model(self, model_path):
        """Carrega o modelo ResNet18 com a arquitetura correta"""
        print(f"Carregando modelo de: {model_path}")
        
        try:
            # Carregar checkpoint
            checkpoint = torch.load(model_path, map_location=self.device)
            print(f"Checkpoint carregado. Tipo: {type(checkpoint)}")
            
            # Extrair informações do checkpoint
            if isinstance(checkpoint, dict):
                # Verificar se tem dataset_info para pegar as classes corretas
                if 'dataset_info' in checkpoint:
                    dataset_info = checkpoint['dataset_info']
                    if 'splits' in dataset_info and 'train' in dataset_info['splits']:
                        saved_classes = dataset_info['splits']['train']['classes']
                        print(f"Classes encontradas no checkpoint: {saved_classes}")
                        self.classes = saved_classes
                
                # Verificar informações de treinamento
                if 'training_info' in checkpoint:
                    training_info = checkpoint['training_info']
                    print(f"Informações de treinamento: {training_info}")
                
                # Pegar state_dict
                if 'model_state_dict' in checkpoint:
                    state_dict = checkpoint['model_state_dict']
                    print("Usando 'model_state_dict'")
                else:
                    print("❌ 'model_state_dict' não encontrado no checkpoint")
                    return None
            else:
                print("❌ Checkpoint não é um dicionário")
                return None
            
            # Detectar número de classes do checkpoint
            num_classes = len(self.classes)
            
            # Verificar pela última camada linear do ResNet18
            if 'fc.weight' in state_dict:
                detected_classes = state_dict['fc.weight'].shape[0]
                print(f"Detectado {detected_classes} classes pela camada fc.weight")
                num_classes = detected_classes
            elif 'fc.4.weight' in state_dict:
                detected_classes = state_dict['fc.4.weight'].shape[0]
                print(f"Detectado {detected_classes} classes pela camada fc.4.weight")
                num_classes = detected_classes
            
            print(f"Criando ResNet18 com {num_classes} classes...")
            
            # USAR RESNET18 (a arquitetura que foi realmente treinada)
            model = models.resnet18(weights=None)
            
            # Analisar a estrutura da camada fc do checkpoint
            fc_layers = {}
            for key in state_dict.keys():
                if key.startswith('fc.'):
                    layer_num = key.split('.')[1]
                    if layer_num not in fc_layers:
                        fc_layers[layer_num] = {}
                    fc_layers[layer_num][key] = state_dict[key].shape
            
            print(f"📋 Estrutura da camada fc encontrada:")
            for layer_num in sorted(fc_layers.keys()):
                print(f"  fc.{layer_num}: {fc_layers[layer_num]}")
            
            # Construir a camada fc baseada no checkpoint
            if 'fc.0.weight' in state_dict and state_dict['fc.0.weight'].shape == torch.Size([256, 512]):
                # Estrutura: 512 -> 256 -> num_classes
                print("Detectada estrutura: 512 -> 256 -> classes")
                model.fc = nn.Sequential(
                    nn.Linear(512, 256),           # fc.0
                    nn.BatchNorm1d(256),           # fc.1  
                    nn.ReLU(inplace=True),         # fc.2 (não tem parâmetros)
                    nn.Dropout(0.5),               # fc.3 (não tem parâmetros)
                    nn.Linear(256, num_classes)    # fc.4
                )
            elif 'fc.weight' in state_dict:
                # Estrutura simples: 512 -> num_classes
                print("Detectada estrutura simples: 512 -> classes")
                model.fc = nn.Linear(512, num_classes)
            else:
                print("❌ Estrutura da camada fc não reconhecida")
                return None
            
            # Debug das chaves
            model_keys = set(model.state_dict().keys())
            checkpoint_keys = set(state_dict.keys())
            
            print(f"Chaves no modelo ResNet18: {len(model_keys)}")
            print(f"Chaves no checkpoint: {len(checkpoint_keys)}")
            
            # Verificar compatibilidade
            missing_keys = checkpoint_keys - model_keys
            extra_keys = model_keys - checkpoint_keys
            
            if len(missing_keys) == 0 and len(extra_keys) == 0:
                print("✅ Arquiteturas são 100% compatíveis!")
            else:
                print(f"⚠️  Diferenças na arquitetura:")
                print(f"   - Chaves ausentes no modelo: {len(extra_keys)}")
                print(f"   - Chaves extras no checkpoint: {len(missing_keys)}")
            
            # Carregar state_dict
            print("Carregando state_dict no ResNet18...")
            try:
                model.load_state_dict(state_dict, strict=True)
                print("✅ State dict carregado com sucesso (strict=True)!")
            except RuntimeError as strict_error:
                print(f"⚠️  Erro com strict=True: {str(strict_error)[:200]}...")
                try:
                    # Tentar com strict=False
                    missing_keys, unexpected_keys = model.load_state_dict(state_dict, strict=False)
                    print(f"✅ State dict carregado com strict=False")
                    if missing_keys:
                        print(f"   - Chaves ausentes: {len(missing_keys)}")
                        if len(missing_keys) <= 5:
                            for key in missing_keys:
                                print(f"     * {key}")
                    if unexpected_keys:
                        print(f"   - Chaves inesperadas: {len(unexpected_keys)}")
                        if len(unexpected_keys) <= 5:
                            for key in unexpected_keys:
                                print(f"     * {key}")
                except Exception as e:
                    print(f"❌ Erro mesmo com strict=False: {e}")
                    return None
            
            # Configurar modelo para inferência
            model.to(self.device)
            model.eval()
            
            print("✅ Modelo configurado para inferência!")
            
            # Mostrar informações do modelo
            total_params = sum(p.numel() for p in model.parameters())
            trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
            print(f"Parâmetros totais: {total_params:,}")
            print(f"Parâmetros treináveis: {trainable_params:,}")
            
            return model
            
        except Exception as e:
            print(f"❌ Erro ao carregar modelo: {e}")
            import traceback
            traceback.print_exc()
            return None

    def predict_single_image(self, image_path):
        """Faz predição para uma única imagem"""
        if self.model is None:
            print("Modelo não carregado!")
            return None
        
        try:
            # Carregar e preprocessar a imagem
            print(f"Carregando imagem: {image_path}")
            image = Image.open(image_path).convert('RGB')
            print(f"Tamanho original: {image.size}")
            
            input_tensor = self.transform(image).unsqueeze(0).to(self.device)
            print(f"Tensor preprocessado: {input_tensor.shape}")
            
            # Fazer predição
            with torch.no_grad():
                outputs = self.model(input_tensor)
                probabilities = F.softmax(outputs[0], dim=0)
                
                # Obter predição principal
                predicted_idx = torch.argmax(probabilities).item()
                confidence = probabilities[predicted_idx].item()
                predicted_class = self.classes[predicted_idx] if predicted_idx < len(self.classes) else f"Classe_{predicted_idx}"
                
                # Obter top 3 predições
                top3_probs, top3_indices = torch.topk(probabilities, min(3, len(self.classes)))
                
                return {
                    'predicted_class': predicted_class,
                    'confidence': confidence,
                    'predicted_idx': predicted_idx,
                    'all_probabilities': {self.classes[i]: probabilities[i].item() 
                                        for i in range(min(len(self.classes), len(probabilities)))},
                    'top3': [(self.classes[idx.item()], prob.item()) 
                            for idx, prob in zip(top3_indices, top3_probs)]
                }
        
        except Exception as e:
            print(f"Erro na predição: {e}")
            import traceback
            traceback.print_exc()
            return None

    def predict_batch_images(self, image_paths):
        """Faz predição para múltiplas imagens"""
        results = []
        for img_path in image_paths:
            result = self.predict_single_image(img_path)
            if result:
                results.append({
                    'image_path': img_path,
                    'prediction': result
                })
        return results


# Teste simples
if __name__ == "__main__":
    try:
        # Caminho para o modelo
        model_path = "../outputs/modelo_AGRO_GESF2.pth"
        
        # Inicializar preditor
        predictor = PlantDiseasePredictor(model_path, device='cpu')
        
        print("\n=== MODELO CARREGADO COM SUCESSO ===")
        
        # Teste com uma imagem se existir
        test_image_path = "./teste19.jpg"
        
        try:
            print(f"\n=== TESTANDO IMAGEM: {test_image_path} ===")
            result = predictor.predict_single_image(test_image_path)
            
            if result:
                print(f"\n🎯 RESULTADO DA PREDIÇÃO:")
                print(f"Classe predita: {result['predicted_class']}")
                print(f"Confiança: {result['confidence']:.4f} ({result['confidence']*100:.2f}%)")
                print(f"Índice da classe: {result['predicted_idx']}")
                
                print(f"\n📊 TOP 3 PREDIÇÕES:")
                for i, (classe, prob) in enumerate(result['top3'], 1):
                    print(f"  {i}. {classe:<20}: {prob:.4f} ({prob*100:.2f}%)")
                
                print(f"\n📊 TODAS AS PROBABILIDADES:")
                for classe, prob in result['all_probabilities'].items():
                    print(f"  {classe:<20}: {prob:.4f} ({prob*100:.2f}%)")
            else:
                print("❌ Falha na predição")
                
        except FileNotFoundError:
            print(f"⚠️  Imagem de teste não encontrada: {test_image_path}")
            print("Para testar uma imagem, use:")
            print("result = predictor.predict_single_image('caminho/para/sua/imagem.jpg')")
        except Exception as img_error:
            print(f"❌ Erro ao processar imagem: {img_error}")
        
    except Exception as e:
        print(f"❌ Erro geral: {e}")
        import traceback
        traceback.print_exc()