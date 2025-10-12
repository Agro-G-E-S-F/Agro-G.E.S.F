import torch
import torch.nn as nn
import torch.nn.functional as F

class SEBlock(nn.Module):
    def __init__(self, channels, reduction=16):
        super(SEBlock, self).__init__()
        self.avg_pool = nn.AdaptiveAvgPool2d(1)
        self.fc = nn.Sequential(
            nn.Linear(channels, channels//reduction, bias=False),
            nn.ReLU(inplace=True),
            nn.Linear(channels//reduction, channels, bias=False),
            nn.Sigmoid(),
        )
        
    def forward(self, x):
        b, c, _, _ = x.size()
        y = self.avg_pool(x).view(b, c)
        y = self.fc(y).view(b, c, 1, 1)
        return x * y.expand_as(x)

class CNN(nn.Module):
    def __init__(self, num_classes,dropout_rate=0.3):
        super(CNN, self).__init__()
        
        self.features = nn.Sequential(
            # Primeira camada 
            nn.Conv2d(3, 64, kernel_size=3,  padding=1, bias=False),
            nn.BatchNorm2d(64),
            nn.ReLU(inplace=True),

            # Segunda camada 
            nn.Conv2d(64, 96, kernel_size=3,  padding=1, bias=False),
            nn.BatchNorm2d(96),
            nn.SiLU(inplace=True),
            nn.MaxPool2d(2, 2),
            nn.Dropout2d(dropout_rate * 0.3),

            # Terceira camada 
            nn.Conv2d(96, 128, kernel_size=3, padding=1, bias=False),
            nn.BatchNorm2d(128),
            nn.SiLU(inplace=True),
            nn.MaxPool2d(2, 2),
            nn.Dropout2d(dropout_rate * 0.4),

            # Quarta camada 
            nn.Conv2d(128, 192, kernel_size=3, padding=1, bias=False),
            nn.BatchNorm2d(192),
            nn.SiLU(inplace=True),
            nn.Dropout2d(dropout_rate*0.45),

            # Quinta camada 
            nn.Conv2d(192, 256, kernel_size=3, padding=1, bias=False),
            nn.BatchNorm2d(256),
            nn.SiLU(inplace=True),
            nn.MaxPool2d(2, 2),
            nn.Dropout2d(dropout_rate*0.5),

            # Sexta camada 
            nn.Conv2d(256, 320, kernel_size=3,padding=1, bias=False),
            nn.BatchNorm2d(320),
            nn.SiLU(inplace=True),
            nn.MaxPool2d(2, 2),
            nn.Dropout2d(dropout_rate*0.6),

            nn.Conv2d(320,384,kernel_size=3,padding=1,bias=False),
            nn.BatchNorm2d(384),
            nn.SiLU(inplace=True),
            nn.MaxPool2d(2, 2),
            nn.Dropout2d(dropout_rate*0.6),

            nn.Conv2d(384,512,kernel_size=3,padding=1,bias=False),
            nn.BatchNorm2d(512),
            nn.SiLU(inplace=True),
            SEBlock(512,32),
            nn.Dropout2d(dropout_rate*0.65),

            nn.Conv2d(512,640, kernel_size=3,padding=1,bias=False),
            nn.BatchNorm2d(640),
            nn.SiLU(inplace=True),
            nn.MaxPool2d(2,2,),
            nn.Dropout2d(dropout_rate*0.7),

            nn.Conv2d(640,768,kernel_size=3,padding=1,bias=False),
            nn.BatchNorm2d(768),
            nn.SiLU(inplace=True),
            SEBlock(768,48),

            nn.AdaptiveAvgPool2d(1),
            nn.Dropout2d(dropout_rate),
        )
                
        self.classifier = nn.Sequential(
            nn.Flatten(),
            nn.Linear(768, 512, bias=False),
            nn.BatchNorm1d(512),
            nn.SiLU(inplace=True),
            nn.Dropout(dropout_rate),

            nn.Linear(512,256,bias=False),
            nn.BatchNorm1d(256),
            nn.SiLU(inplace=True),
            nn.Dropout(dropout_rate*0.7),

            nn.Linear(256,num_classes)
        )
        
        self._initialize_weights()         

    def freeze_low_features(self):
        layers_to_freeze = list(self.features)[:25]
        for layer in layers_to_freeze:
            if hasattr(layer,"parameters"):
                for param in layer.parameters():
                    param.requires_grad = False

    def unfreeze_low_features(self):
        layers_to_train = list(self.features)[25:]
        for layer in layers_to_train:
            if hasattr(layer,"parameters"):
                for param in layer.parameters():
                    param.requires_grad = True



    def _initialize_weights(self):
        for m in self.modules():
            if isinstance(m, nn.Conv2d):
                nn.init.kaiming_normal_(m.weight, mode='fan_out', nonlinearity='relu')
            elif isinstance(m, (nn.BatchNorm2d, nn.BatchNorm1d)):
                nn.init.constant_(m.weight, 1)
                nn.init.constant_(m.bias, 0)
            elif isinstance(m, nn.Linear):
                nn.init.normal_(m.weight, 0, 0.01)
                if m.bias is not None:
                    nn.init.constant_(m.bias, 0)

    def forward(self, x):
        x = self.features(x)
        x = self.classifier(x)
        return x