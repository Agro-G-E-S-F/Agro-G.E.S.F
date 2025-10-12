import torch
import torch.nn as nn
import torch.nn.functional as F
def debug_model_checkpoint(model_path):
    """Debug detalhado do checkpoint"""
    print("=" * 60)
    print("🔍 DEBUG DETALHADO DO CHECKPOINT")
    print("=" * 60)
    
    try:
        checkpoint = torch.load(model_path, map_location='cpu')
        print(f"✅ Checkpoint carregado com sucesso")
        print(f"Tipo do checkpoint: {type(checkpoint)}")
        
        if isinstance(checkpoint, dict):
            print(f"\n📋 CHAVES PRINCIPAIS DO CHECKPOINT:")
            for key in checkpoint.keys():
                value = checkpoint[key]
                if isinstance(value, torch.Tensor):
                    print(f"  {key}: Tensor {value.shape}")
                elif isinstance(value, dict):
                    print(f"  {key}: Dict com {len(value)} itens")
                else:
                    print(f"  {key}: {type(value)} - {str(value)[:100]}")
            
            # Verificar se tem informações de treinamento
            if 'epoch' in checkpoint:
                print(f"\n📈 INFORMAÇÕES DE TREINAMENTO:")
                print(f"  Época: {checkpoint['epoch']}")
                
            if 'loss' in checkpoint:
                print(f"  Loss: {checkpoint['loss']}")
                
            if 'accuracy' in checkpoint or 'acc' in checkpoint:
                acc_key = 'accuracy' if 'accuracy' in checkpoint else 'acc'
                print(f"  Acurácia: {checkpoint[acc_key]}")
            
            if 'optimizer_state_dict' in checkpoint:
                print(f"  ✅ Tem state_dict do otimizador")
            
            # Examinar o state_dict do modelo
            if 'model_state_dict' in checkpoint:
                state_dict = checkpoint['model_state_dict']
                print(f"\n🧠 ANÁLISE DO MODEL_STATE_DICT:")
                print(f"  Total de parâmetros: {len(state_dict)}")
                
                # Verificar algumas camadas importantes
                important_layers = []
                for key in state_dict.keys():
                    if any(x in key for x in ['conv', 'fc', 'classifier', 'features']):
                        tensor = state_dict[key]
                        if 'weight' in key:
                            # Calcular estatísticas dos pesos
                            mean_val = tensor.mean().item()
                            std_val = tensor.std().item()
                            min_val = tensor.min().item()
                            max_val = tensor.max().item()
                            
                            important_layers.append({
                                'layer': key,
                                'shape': tensor.shape,
                                'mean': mean_val,
                                'std': std_val,
                                'min': min_val,
                                'max': max_val
                            })
                
                print(f"\n🔢 ESTATÍSTICAS DOS PESOS (primeiras 10 camadas):")
                for layer_info in important_layers[:10]:
                    print(f"  {layer_info['layer']:<40}: shape={layer_info['shape']}")
                    print(f"    mean={layer_info['mean']:.6f}, std={layer_info['std']:.6f}, min={layer_info['min']:.6f}, max={layer_info['max']:.6f}")
                
                # Verificar se os pesos parecem treinados
                print(f"\n🎯 ANÁLISE DE TREINAMENTO:")
                weights_seem_trained = False
                
                for layer_info in important_layers:
                    # Se a média não é próxima de 0 e std não é muito pequeno, provavelmente foi treinado
                    if abs(layer_info['mean']) > 0.001 or layer_info['std'] > 0.1:
                        weights_seem_trained = True
                        break
                
                if weights_seem_trained:
                    print("  ✅ Os pesos parecem ter sido treinados (valores não-zero significativos)")
                else:
                    print("  ⚠️  Os pesos parecem inicializados mas não treinados")
                
                # Verificar a última camada especificamente
                last_layer_keys = [k for k in state_dict.keys() if 'classifier' in k and 'weight' in k]
                if last_layer_keys:
                    last_layer = last_layer_keys[-1]
                    last_weights = state_dict[last_layer]
                    print(f"\n🎯 ÚLTIMA CAMADA ({last_layer}):")
                    print(f"  Shape: {last_weights.shape}")
                    print(f"  Número de classes detectado: {last_weights.shape[0]}")
                    print(f"  Mean: {last_weights.mean():.6f}")
                    print(f"  Std: {last_weights.std():.6f}")
        
        else:
            print("❌ Checkpoint não é um dicionário - formato inesperado")
            
    except Exception as e:
        print(f"❌ Erro ao carregar checkpoint: {e}")
        import traceback
        traceback.print_exc()

def test_model_inference():
    """Testa se o modelo está fazendo inferência corretamente"""
    print("\n" + "=" * 60)
    print("🧪 TESTE DE INFERÊNCIA")
    print("=" * 60)
    
    from teste_image_cpu import PlantDiseasePredictor
    
    try:
        predictor = PlantDiseasePredictor("../outputs/modelo_resnet.pth", device='cpu')
        
        if predictor.model is None:
            print("❌ Modelo não carregado")
            return
        
        # Criar uma imagem de teste aleatória
        test_input = torch.randn(1, 3, 224, 224)
        
        print(f"📊 TESTE COM INPUT ALEATÓRIO:")
        print(f"  Input shape: {test_input.shape}")
        
        with torch.no_grad():
            output = predictor.model(test_input)
            probabilities = F.softmax(output[0], dim=0)
            
            print(f"  Output shape: {output.shape}")
            print(f"  Output raw: {output[0][:5].tolist()}")  # Primeiros 5 valores
            print(f"  Probabilities: {probabilities[:5].tolist()}")  # Primeiros 5 valores
            
            # Verificar se todas as probabilidades são iguais
            prob_values = probabilities.tolist()
            all_equal = all(abs(p - prob_values[0]) < 1e-6 for p in prob_values)
            
            if all_equal:
                print(f"  ⚠️  PROBLEMA: Todas as probabilidades são iguais ({prob_values[0]:.6f})")
                print(f"      Isso indica que o modelo não foi treinado ou pesos não foram carregados")
            else:
                print(f"  ✅ As probabilidades são diferentes - modelo parece OK")
                
    except Exception as e:
        print(f"❌ Erro no teste: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    # 1. Debug do checkpoint
    debug_model_checkpoint("../outputs/modelo_resnet.pth")
    
    # 2. Teste de inferência
    test_model_inference()