##Código correspondente a fazer a organização do dataset

import os
import random
import shutil

## Definindo as variáveis constantes do ambiente
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
RAW_DIR = os.path.join(BASE_DIR, "data/raw")
TRAIN_DIR = os.path.join(BASE_DIR, "data/train")
VAL_DIR = os.path.join(BASE_DIR, "data/val")


## Função responsável por dividir os dados
def split_data(source_dir, train_dir, val_dir, val_ratio=0.2):
    classes = os.listdir(source_dir)
    for cls in classes:
        cls_path = os.path.join(source_dir, cls)
        images = os.listdir(cls_path)
        random.shuffle(images)

        n_val = int(len(images) * val_ratio)
        val_img = images[:n_val]
        train_imgs = images[n_val:]

        os.makedirs(os.path.join(train_dir, cls), exist_ok=True)
        os.makedirs(os.path.join(val_dir, cls), exist_ok=True)

        for img in train_imgs:
            shutil.copy(os.path.join(cls_path, img), os.path.join(train_dir, cls, img))
        for img in val_img:
            shutil.copy(os.path.join(cls_path, img), os.path.join(val_dir, cls, img))

if __name__ == "__main__":
    split_data(RAW_DIR, TRAIN_DIR, VAL_DIR, val_ratio=0.2)
