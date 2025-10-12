from data.data import get_dataloaders
from torchvision import models
from training.train import train_model
from utils.utils import (
    save_checkpoint,
    calculate_accuracy, 
    TrainingMonitor,
    save_dataset_structure,
    save_model_complete,
    
)
from models.cnn import CNN
import torch
import torch.nn as nn
import torch.optim as optim
import os
from torch.utils.data import Subset
import random
from PIL import ImageFile
ImageFile.LOAD_TRUNCATED_IMAGES = True

data_dir = os.path.join(os.path.dirname(__file__),'..','data')
data_dir = os.path.abspath(data_dir)

def get_model(num_classes,class_names):
    model = CNN(num_classes=len(class_names))
    
    in_features = model.classifier[-1].in_features
    model.classifier[-1] = nn.Linear(
        in_features,
        len(class_names)
    )
    return model
def main():
    ## Parte por identificar o dispositivo sendo usado
    
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print('\n Salvando estrutura do dataset')
    dataset_info = save_dataset_structure(data_dir,'dataset_structure.json')    
    ## Definindo o tamanho do batch
    batch_size = 64
    num_workers = 4
    print(f'Tamanho do batch: {batch_size}')
    #Definindo quantas épocas o modelo vai treinar
    
    num_epochs = 60
    ##Carregando da função data.py os dataloaders, o tamanho dos datasets e o nome das classes
    try:
        dataloaders,dataset_sizes,class_names = get_dataloaders(data_dir, batch_size=batch_size,num_workers=num_workers)
        print(f'Classes:{class_names}')
        print(f'Dataset sizes:{dataset_sizes}')
    except Exception as e:
        print(f'Erro ao carregar os dataloaders: {e}')
        return
    
    ## Definindo o modelo com a estrutura da CNN dentor de cnn.py
    model = get_model(num_classes=len(class_names),class_names=class_names)
    print(f'Modelo: {model} com {len(class_names)} classes')
    ## Definindo a função correpsondente a cálculo de loss
    criterion = nn.CrossEntropyLoss()
    
    ## Definindo o optimizador
    optimizer = optim.AdamW(
        filter(lambda p: p.requires_grad,model.parameters()),
        lr=1e-4,
        weight_decay=1e-4
    )
    print("\n"+"-"*20)
    
    try:
        test_tensor = torch.randn(1, 3, 224, 224)
        if device.type == 'cuda':
            test_tensor = test_tensor.to(device)
            print("GPU funcionando ")
        else:
            print('Deu ruim')
                        
    except Exception as e:
        print(f"❌ Erro no teste do device: {e}")
        print("Forçando uso de CPU...")
        device = torch.device('cpu')
    try:
        model,history = train_model(
            model,
            dataloaders,
            dataset_sizes,
            criterion,optimizer,
            device,
            num_epochs=num_epochs,
            monitor_plots=True,
            checkpoint_dir='checkpoint'
        )
        
        sucess_model = save_model_complete(
            model=model,
            save_path="modelo_AGRO_GESF.pth",
            dataset_info=dataset_info,
            optimizer=optimizer,
            epoch=num_epochs,
            history=history,
            best_acc=max(history['val_acc'])if history['val_acc'] else 0
        )
        if sucess_model:
            print('Modelo salvo com sucesso')
        else:
            print('Erro na hora de salvar o modelo')
    except Exception as e:
        print(f'Erro ao treinar o modelo: {e}')
        return

    print("\n"+"-"*20)
    
    def count_parameters(model):
        total = sum(p.numel() for p in model.parameters())
        trainable = sum(p.numel() for p in model.parameters() if p.requires_grad)
        return total, trainable

    total_params, trainable_params = count_parameters(model)
    print(f"Parâmetros totais: {total_params:,}")
    print(f"Parâmetros treináveis: {trainable_params:,}")
    print(f"Tamanho estimado: {(total_params * 4) / (1024*1024):.2f} MB")

if __name__ == "__main__":
    main()