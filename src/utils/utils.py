## Código responsável por funções auxiliares
## Como:
##  SALVAR MODELO
## MONTITORAR MÉTRICAS

import torch
import time
import copy
import os
from tqdm import tqdm
import matplotlib.pyplot as plt
import json
from datetime import datetime
def save_checkpoint(model,optimizer,epoch,best_acc=None,checkpoint_dir="checkpoints"):
    if not os.path.exists(checkpoint_dir):
        os.makedirs(checkpoint_dir)

    checkpoint_path = os.path.join(checkpoint_dir, f"epoch_{epoch+1}.pth")

    last_path = os.path.join(checkpoint_dir, "last.pth")

    checkpoint={
        "epoch":epoch,
        "model_state_dict":model.state_dict(),
        "optimizer_state_dict":optimizer.state_dict(),
        "best_acc":best_acc,
        "froze_params":[not p.requires_grad for p in model.parameters()],
    }
    torch.save(checkpoint, checkpoint_path)
    torch.save(checkpoint, last_path)
    print(f"Checkpoint saved to {checkpoint_path}")

def calculate_accuracy(outputs,labels):
    _, preds = torch.max(outputs,1)
    correct = torch.sum(preds == labels).item()
    return correct /labels.size(0)

## Função para monitorar o treinamento
class TrainingMonitor():
    
    def __init__(self,save_plots,plot_dir='training_plots'):
        self.save_plots = save_plots
        self.plot_dir = plot_dir
        self.start_time = None
        
        #Criando o diretório para salvar os gráficos
        if save_plots and not os.path.exists(plot_dir):
            os.makedirs(plot_dir)
            
    def plot_metrics(self, history, epoch):
        if not self.save_plots:
            return
        
        # Verificar se há dados suficientes para plotar
        if (len(history.get('train_loss', [])) == 0 or 
            len(history.get('val_loss', [])) == 0):
            print("Dados insuficientes para plotar. Pulando...")
            return
        
        # Verificar se train e val têm o mesmo tamanho
        train_len = len(history['train_loss'])
        val_len = len(history['val_loss'])
        
        if train_len != val_len:
            print(f"Aviso: Tamanhos diferentes - Train: {train_len}, Val: {val_len}")
            # Usar o menor tamanho para evitar erro
            min_len = min(train_len, val_len)
            if min_len == 0:
                return
        else:
            min_len = train_len
        
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 5))
        
        # Plotando o gráfico de perda
        epochs_range = range(1, min_len + 1)
        ax1.plot(epochs_range, history['train_loss'][:min_len], 'bo-', 
                label='Training Loss', linewidth=2)
        ax1.plot(epochs_range, history['val_loss'][:min_len], 'ro-', 
                label='Validation Loss', linewidth=2)
        ax1.set_title('Training and Validation Loss')
        ax1.set_xlabel('Epochs')
        ax1.set_ylabel('Loss')
        ax1.legend()
        ax1.grid(True, alpha=0.3)
        
        # Plotando o gráfico de acurácia
        ax2.plot(epochs_range, history['train_acc'][:min_len], 'bo-', 
                label='Training Accuracy', linewidth=2)
        ax2.plot(epochs_range, history['val_acc'][:min_len], 'ro-', 
                label='Validation Accuracy', linewidth=2)
        ax2.set_title('Training and Validation Accuracy')
        ax2.set_xlabel('Epochs')
        ax2.set_ylabel('Accuracy')
        ax2.legend()
        ax2.grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(f'{self.plot_dir}/training_progress_epoch_{epoch+1}.png', 
                    dpi=150, bbox_inches='tight')
        plt.close()
    
    #Função para formatar o tempo
    def format_time(self,seconds):
        
        hours = int(seconds // 3600)
        minutes = int((seconds % 3600) // 60)
        seconds = int(seconds % 60)
        return f"{hours:02d}:{minutes:02d}:{seconds:02d}"
    

    ## Função para logar o resumo da epoca
    def log_epoch_summary(self, epoch, num_epochs, history, best_acc, epoch_time):
        print(f"\n{'='*50}")
        print(f"Resumo da epoca {epoch+1}/{num_epochs}")
        print(f"{'='*50}")
        print(f"Tempo de treinamento: {self.format_time(epoch_time)}")
        
        if len(history['train_loss']) > 0:
            print(f"Train Loss: {history['train_loss'][-1]:.4f}")
            print(f"Train Acurácia: {history['train_acc'][-1]:.4f}")
        
        if len(history.get('val_loss', [])) > 0:
            print(f"Val Loss: {history['val_loss'][-1]:.4f}")
            print(f"Val Acurácia: {history['val_acc'][-1]:.4f}")
        
        print(f"Melhor acuracia: {best_acc:.4f}")
        
        if self.start_time is not None:
            total_time = time.time() - self.start_time
            remaining_epochs = num_epochs - (epoch + 1)
            if epoch >= 0:
                avg_time_per_epoch = total_time / (epoch + 1)
                eta = avg_time_per_epoch * remaining_epochs
                print(f"Tempo total: {self.format_time(total_time)} | ETA: {self.format_time(eta)}")
        
        print(f"{'='*50}\n")
        
## Função para salvar a estrutura do dataset
def save_dataset_structure(data_dir,save_path='dataset_structure.json'):
    
    dataset_info =  {
        'timestamp':datetime.now().isoformat(),
        'data_directory':data_dir,
        'splits':{}
    }
    for split in ['train','val']:
        split_path = os.path.join(data_dir,split)

        if not os.path.exists(split_path):
            print(f'Slipt {split} não encontrado')
            continue
        classes = sorted([d for d in os.listdir(split_path)
                         if os.path.isdir(os.path.join(split_path,d))])
        split_info={
            'path':split_path,
            'classes':classes,
            'class_to_idx':{cls:idx for idx,cls in enumerate(classes)},
            'idx_to_class':{idx:cls for idx,cls in enumerate(classes)},
            'class_counts':{},
            'total_samples':0     
        }
        
        for class_name in classes:
            class_path = os.path.join(split_path,class_name)
            if os.path.exists(class_path):
                images = [f for f in os.listdir(class_path)
                          if f.lower().endswith(('.jpg','jpeg','png'))]
                split_info['class_counts'][class_name] = len(images)
                split_info['total_samples']+=len(images)
            else:
                split_info['class_counts'][class_name] = 0
        dataset_info['splits'][split] = split_info
        
        print(f'{split.upper()}:')
        print(f'    Total de classes: {len(classes)}')
        print(f'    Total de imagens: {split_info["total_samples"]}')
        
        
        for i, cls in enumerate(classes):
            count = split_info['class_counts'][cls]
        with open(save_path,'w',encoding='utf-8') as f:
            json.dump(dataset_info,f,indent=2,ensure_ascii=False)
        print(f' Estrutura do dataset salva em {save_path}')
        return dataset_info
    
def save_model_complete(model, save_path, dataset_info=None, optimizer=None, 
                       epoch=None, history=None, best_acc=None):
    print(f"Salvando modelo em {save_path}")
    
    model_data = {
        'model_state_dict': model.state_dict(),  
        'model_architecture': {
            'class_name': model.__class__.__name__,
            'num_classes': len(dataset_info['splits']['train']['classes']) if dataset_info else None
        },
        'timestamp': datetime.now().isoformat(),
        'training_info': {
            'epoch': epoch,
            'best_acc': best_acc,
            'history': history,
            'total_params': sum(p.numel() for p in model.parameters())
        }
    }

    if dataset_info is not None:
        model_data['dataset_info'] = dataset_info
    
    if optimizer is not None:
        model_data['optimizer_state_dict'] = optimizer.state_dict()

    model_data['device'] = str(next(model.parameters()).device)
    model_data['is_best'] = (best_acc == max(history['val_acc'])) if history else None
    model_data['model_architecture']['hyperparams'] = {
        'dropout_rate': getattr(model, 'dropout_rate', None)
    }
    torch.save(model_data, save_path)
    print(f"Modelo salvo com {model_data['training_info']['total_params']:,} parâmetros")
    return True