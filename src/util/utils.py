import cv2 as cv
import time

vid = cv.VideoCapture(0)
count = 0

##Função para tirar fotos de 10 em 10 segundo funcionando já
class utils:
    def __init__(self):
        ... 
    def takePicture():
        while True:
            ret, frame = vid.read()
            if not ret:
                print("Erro ao capturar frame!")
                break

            cv.imshow("Camera", frame)

            # salva imagem a cada 10 segundos
            time.sleep(10)
            filename = f"foto_{count}.jpg"
            cv.imwrite(filename, frame)
            print(f"Foto salva: {filename}")
            count += 1

            if cv.waitKey(1) & 0xFF == ord('q'):
                break
    
    def organizePictures(self,img_picture,infos_pd):
        
        infos_org = {}
        
        for index, info in infos_pd:
            #index_org
            ...
        ##Terminar o código para salvar as infos das pragas e doenças e o caminho da imagem no dicionário infos_org