import torch
import time
import copy
import os
from tqdm import tqdm

from utils.utils import save_checkpoint, calculate_accuracy, TrainingMonitor

def train_model(
    model, 
    dataloaders,
    dataset_sizes,
    criterion,
    optimizer,
    device,
    num_epochs,
    monitor_plots=True,
    checkpoint_dir='checkpoint.pth'
    ):
    # Função para treinar um modelo.
    # Argumentos:
      #model: O modelo a ser treinado.
      #dataloaders: Um dicionario com os dataloaders para treino e validação.
      #dataset_sizes: Um dicionario com o tamanho dos conjuntos de treino e validação.
      #criterion: A função de perda a ser utilizada.
      #optimizer: O otimizador a ser utilizado.
      #device: O dispositivo (GPU ou CPU) a ser utilizado.
      #num_epochs: O número de  épocas de treino.
      #monitor_plots: Um booleano indicando se os gráficos de perda e acuracia devem ser monitorados.
      #checkpoint_dir: O diretório onde os checkpoints devem ser salvos.
    
    # Colocando o modelo no dispositivo
    model = model.to(device)
    print(device)
    
    # Monitorando o treinamento
    monitor = TrainingMonitor(save_plots=monitor_plots)
    monitor.start_time = time.time()
    
    if not os.path.exists(checkpoint_dir):
        os.makedirs(checkpoint_dir)
    
    # Váriaveis de controle
    best_model_wts = copy.deepcopy(model.state_dict())
    best_acc = 0.0
    
    history = {
        'train_loss': [],
        'train_acc': [],
        'val_loss': [],
        'val_acc': [],
    }
    
    # Loop de treinamento
    for epoch in range(num_epochs):
        epoch_start_time = time.time()
        print(f'Epoch {epoch+1}/{num_epochs}')
        print('-' * 20)
        
        for phase in ['train', 'val']:
            if phase == 'train':
                model.train()
                desc = "Training"
            else:
                model.eval()
                desc = "Validation"
            
            running_loss = 0.0
            running_corrects = 0
            batch_count = 0
            
            # Progress bar
            phase_loader = tqdm(dataloaders[phase],
                                desc=f'{desc} ({phase})',
                                leave=False,
                                ncols=100
                                )
            
            for inputs, labels in phase_loader:
                inputs = inputs.to(device)
                labels = labels.to(device)
                optimizer.zero_grad()
                
                with torch.set_grad_enabled(phase == 'train'):
                    outputs = model(inputs)
                    loss = criterion(outputs, labels)
                    acc = calculate_accuracy(outputs, labels)
                
                if phase == 'train':
                    loss.backward()
                    optimizer.step()
                
                batch_size = inputs.size(0)
                running_loss += loss.item() * batch_size
                running_corrects += (acc * batch_size)
                batch_count += 1
                
                current_loss = running_loss / (batch_count * batch_size)
                if isinstance(running_corrects, torch.Tensor):
                    current_acc = (running_corrects / (batch_count * batch_size)).item()
                else:
                    current_acc = running_corrects / (batch_count * batch_size)
                
                # Fixed: Added proper precision to format specifiers
                phase_loader.set_postfix({ 
                    'Loss': f'{current_loss:.3f}',
                    'Acc': f'{current_acc:.3f}',
                })
            
            epoch_loss = running_loss / dataset_sizes[phase]
            if isinstance(running_corrects, torch.Tensor):
                epoch_acc = (running_corrects / dataset_sizes[phase]).item()
            else:
                epoch_acc = running_corrects / dataset_sizes[phase]
            
            if phase == 'train':
                history['train_loss'].append(epoch_loss)
                history['train_acc'].append(epoch_acc)
            elif phase == 'val':
                history['val_loss'].append(epoch_loss)
                history['val_acc'].append(epoch_acc)
            
            # Salvar o melhor modelo
            if phase == 'val' and epoch_acc > best_acc:
                best_acc = epoch_acc
                best_model_wts = copy.deepcopy(model.state_dict())
                save_checkpoint(model, optimizer, epoch, best_acc, checkpoint_dir="checkpoints")

        epoch_time = time.time() - epoch_start_time
        
        if len(history['train_loss']) > 0 and len(history['val_loss']) > 0:
            monitor.plot_metrics(history, epoch)
            monitor.log_epoch_summary(epoch, num_epochs, history, best_acc, epoch_time)
        else:
            print("Falta de dados para plotação")
            
        print(f'Melhor val acc: {best_acc:.4f}')

    total_time = time.time() - monitor.start_time
    print(f"Tempo de treinamento: {monitor.format_time(total_time)}")    
    model.load_state_dict(best_model_wts)
    return model, history