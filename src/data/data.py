from torchvision import datasets, transforms
from torch.utils.data import DataLoader
import os

import os
train_dir = "./data/train"  # ajuste o caminho
classes_from_folders = sorted(os.listdir(train_dir))
print("Ordem real das classes (baseada nos diretórios):")
for i, classe in enumerate(classes_from_folders):
    print(f"{i}: {classe}")
    
def get_data_transforms():
    data_transforms = {
        'train': transforms.Compose ([
            transforms.RandomResizedCrop(224),
            transforms.RandomHorizontalFlip(),
            transforms.ColorJitter(brightness=0.2,contrast=0.2,saturation=0.2,hue=0.1),
            transforms.ToTensor(),
            transforms.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225]),
            
        ]),
        
        'val': transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225]),
            
        ]),
    }
    return data_transforms
def get_dataloaders(data_dir,batch_size,num_workers):
    
    train_path = os.path.join(data_dir,'train')
    val_path = os.path.join(data_dir,'val')
    
    print(f"DEBUG - data_dir: {data_dir}")
    print(f"DEBUG - train_path: {train_path}")
    print(f"DEBUG - val_path: {val_path}")
    print(f"DEBUG - train existe: {os.path.exists(train_path)}")
    print(f"DEBUG - val existe: {os.path.exists(val_path)}")

    if not os.path.exists(train_path):
        raise FileNotFoundError(f"Diretório train não encontrado: {train_path}")
    if not os.path.exists(val_path):
        raise FileNotFoundError(f"Diretório val não encontrado: {val_path}")
    
    data_transforms = get_data_transforms()
    image_datasets = {x:datasets.ImageFolder(os.path.join(data_dir,x),data_transforms[x])
                  for x in ['train','val']}
    
    dataloaders = {x:DataLoader(image_datasets[x],batch_size=batch_size,shuffle=True,num_workers=num_workers)
                    for x in ['train','val']}
    
    datasets_size = {x:len(image_datasets[x]) for x in ['train','val']}

    class_names = image_datasets['train'].classes

    return dataloaders,datasets_size,class_names