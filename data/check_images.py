# tools/find_corrupt_images.py
import os
from PIL import Image, UnidentifiedImageError
from tqdm import tqdm

ROOT = "."  # ajuste se necessário
OUT_DIR = "corrupted_images"
os.makedirs(OUT_DIR, exist_ok=True)

def check_and_move(folder):
    moved = 0
    for root, _, files in os.walk(folder):
        for f in files:
            if not f.lower().endswith(('.jpg','jpeg','png')):
                continue
            path = os.path.join(root, f)
            try:
                with Image.open(path) as im:
                    im.verify()  # verifica integridade do arquivo
            except (UnidentifiedImageError, OSError, ValueError) as e:
                print(f"[CORRUPT] {path} -> {e}")
                # mover para pasta de análise
                rel = os.path.relpath(path, ROOT)
                target = os.path.join(OUT_DIR, rel)
                os.makedirs(os.path.dirname(target), exist_ok=True)
                try:
                    os.rename(path, target)
                except Exception as ex:
                    print(f"  Erro ao mover: {ex}")
                moved += 1
    return moved

if __name__ == "__main__":
    total = 0
    for split in ['train','val']:
        p = os.path.join(ROOT, split)
        if os.path.exists(p):
            print("Checando:", p)
            m = check_and_move(p)
            print(f"  Movidos: {m}")
            total += m
    print("Total movidos:", total)
