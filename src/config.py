import os
from pathlib import Path

# Diretório raiz do projeto (vai para o pai de 'src')
BASE_DIR = Path(__file__).resolve().parent.parent

# Principais caminhos
MODEL_PATH = BASE_DIR /"src"/"outputs"/ "Modelo_Folha_90.pth"
OUTPUTS_PATH = BASE_DIR / "src" / "outputs"
CNN_PATH = BASE_DIR /"src" /"models"/"cnn.py"
# Caminho para a pasta de dados
DATA_DIR = BASE_DIR / "data"
TRAIN_DIR = DATA_DIR / "train"
VAL_DIR = DATA_DIR / "val"
RAW_DIR = DATA_DIR / "raw"