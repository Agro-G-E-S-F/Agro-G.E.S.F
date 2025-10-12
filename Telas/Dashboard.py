import customtkinter as ctk
from PIL import Image, ImageDraw
from pathlib import Path
from Cor_Imgs import cores, caminho_imgs
from SobreNos import abrir_sobre_nos
from Grafico_Pragas import Abrir_Grafico_Praga


# centralizar janelas
def centralizar_janela(window, width, height):
    screen_w = window.winfo_screenwidth()
    screen_h = window.winfo_screenheight()
    x = (screen_w - width) // 2
    y = (screen_h - height) // 2
    window.geometry(f"{width}x{height}+{x}+{y}")

# Função para criar imagem com bordas arredondadas
def imagem_bordas_arredondadas(caminho_imagem, tamanho=(400, 400), raio=20):
    # Abre e redimensiona a imagem
    img = Image.open(caminho_imagem).resize(tamanho).convert("RGBA")
    
    # Cria uma máscara com cantos arredondados
    mascara = Image.new('L', tamanho, 0)
    draw = ImageDraw.Draw(mascara)
    
    # Desenha um retângulo com cantos arredondados
    draw.rounded_rectangle((0, 0, tamanho[0], tamanho[1]), radius=raio, fill=255)
    
    # Aplica a máscara no canal alpha
    img.putalpha(mascara)
    
    return img
    
# faz um pré-carregamento das imagens 
def carregar_ctk_imagem(path, size):
    if Path(path).is_file():
        img = Image.open(path)
        return ctk.CTkImage(light_image=img, size=size)
    return None

# Função para carregar imagem com bordas arredondadas para CTk
def carregar_ctk_imagem_arredondada(path, size, raio=20):
    if Path(path).is_file():
        img_arredondada = imagem_bordas_arredondadas(path, size, raio)
        return ctk.CTkImage(light_image=img_arredondada, size=size)
    return None

frame_foto_praga = None
label_foto = None
label_contador = None
fotos_detectadas = []  # lista de imagens
foto_index = 0         # índice atual
btn_left = None
btn_right = None

def criar_frame_foto(master):
    global frame_foto_praga, label_foto, btn_left, btn_right, frame_desc_praga

    frame_foto_praga = ctk.CTkFrame(
        master=master,
        width=450,
        height=500,
        corner_radius=15,
        fg_color=cores['Fundo_Menu']
    )
    frame_foto_praga.place(relx=0.25, rely=0.45, anchor="center")

    # Label que mostra a foto
    label_foto = ctk.CTkLabel(master=frame_foto_praga, text="")
    label_foto.pack(expand=True, padx=10, pady=10)

    # carrega as imagens das setas
    seta_direita = carregar_ctk_imagem(caminho_imgs['Seta_dash_direita'], (60,60))
    seta_esquerda = carregar_ctk_imagem(caminho_imgs['Seta_dash_esquerda'], (60,60))
    
    # Botão seta esquerda
    btn_left = ctk.CTkButton(
        master=master,
        image=seta_esquerda,
        fg_color="transparent",
        text="",
        width=60,
        height=60,
        command=mostrar_anterior
    )
    btn_left.place(x=0, y=0)
    btn_left.configure(hover=False)

    # Botão seta direita
    btn_right = ctk.CTkButton(
        master=master,
        image=seta_direita,
        fg_color="transparent",
        text="",
        width=60,
        height=60,
        command=mostrar_proxima
    )
    btn_right.place(x=0, y=0)
    btn_right.configure(hover=False)


    # Distâncias configuráveis
    dist_setas = 20       # distância horizontal das setas até o frame da foto
    dist_frame_desc = 60  # distância horizontal do frame de descrição em relação à foto
    dist_topo_desc = 30   # distância vertical do topo da descrição em relação ao topo da foto

    def reposicionar_elementos(event=None):
        # Pega coordenadas do frame da foto
        x_frame = frame_foto_praga.winfo_x()
        y_frame = frame_foto_praga.winfo_y()
        w_frame = frame_foto_praga.winfo_width()
        h_frame = frame_foto_praga.winfo_height()
        
        # --- Posicionar setas ---
        y_centro = y_frame + h_frame // 2
        btn_left.place(x=x_frame - dist_setas, y=y_centro, anchor="e")
        btn_right.place(x=x_frame + w_frame + dist_setas, y=y_centro, anchor="w")


    # Vincula atualização sempre que a janela mudar
    master.bind("<Configure>", reposicionar_elementos)


def exibir_foto_praga(caminho_img):
    global label_foto
    img = carregar_ctk_imagem_arredondada(caminho_img, (400, 400), raio=30)  # raio de 30 pixels
    if img:
        label_foto.configure(image=img)
        label_foto.image = img 

def mostrar_proxima():
    """Mostra a próxima imagem da lista"""
    global foto_index
    if fotos_detectadas:
        foto_index = (foto_index + 1) % len(fotos_detectadas)
        exibir_foto_praga(fotos_detectadas[foto_index])


def mostrar_anterior():
    """Mostra a imagem anterior da lista"""
    global foto_index
    if fotos_detectadas:
        foto_index = (foto_index - 1) % len(fotos_detectadas)
        exibir_foto_praga(fotos_detectadas[foto_index])
# -------------------------------------------------------------------


def main():
    ctk.set_appearance_mode("light")
    ctk.set_default_color_theme("blue")
    
    from perfil import Abrir_Perfil # import local
    
    dashBoard = ctk.CTk()
    dashBoard.title("Dashboard")
    centralizar_janela(dashBoard, 1200, 700)
    dashBoard.configure(fg_color=cores['cor_fundo'])
    
    if Path(caminho_imgs['icon']).is_file():
        dashBoard.iconbitmap(caminho_imgs['icon'])
        
    # Imagens visuais
    Folha_inferior = carregar_ctk_imagem(caminho_imgs['folha_dash_menu_inferior'], (250,250))
    Folha_superior = carregar_ctk_imagem(caminho_imgs['folha_dash_menu_superior'], (250,250))
    
    if Folha_inferior:
        Folha_inferior_label = ctk.CTkLabel(
            master=dashBoard,
            image=Folha_inferior,
            text=""
        )
        Folha_inferior_label.place(relx=1.0, rely=1.0, anchor="se")

    if Folha_superior:
        Folha_superior_label = ctk.CTkLabel(
            master=dashBoard,
            image=Folha_superior,
            text=""
        )
        Folha_superior_label.place(relx=0.0, rely=0.0, anchor="nw")
    
    # Título
    Titulo_Praga_detectada = ctk.CTkLabel(
        master=dashBoard,
        text_color=cores['branco'],
        font=("Poppins", 25, "bold"),
        text="Foto da Praga detectada:"
    ).place(relx=0.1, rely=0.1, anchor="w")
    
    # Botões principais
    Confirm_buttom = ctk.CTkButton(
        master=dashBoard,
        width=220,
        height=60,
        corner_radius=15,
        fg_color=cores['verde_primario'],
        text="É uma praga",
        hover_color=cores['verde_primario_hover'],
        font=("Lato", 22, "bold")
    )
    Confirm_buttom.place(relx=0.88, rely=0.55, anchor="center")
    
    Cancel_buttom = ctk.CTkButton(
        master=dashBoard,
        width=220,
        height=60,
        corner_radius=15,
        fg_color=cores['vermelho_primario'],
        hover_color=cores['vermelho_secundario_hover'],
        font=("Lato", 22, "bold"),
        text="Alarme Falso"
    )
    Cancel_buttom.place(relx=0.60, rely=0.55, anchor="center")
    
    # Frame da descrição da praga 
    frame_desc_praga = ctk.CTkFrame(
        master=dashBoard,
        width=550,
        height=200,
        corner_radius=10,
        fg_color= cores['Fundo_azul']
    )
    frame_desc_praga.place(relx= 0.51, rely=0.21) 

    #frame de descrição da praga
    titulo_label = ctk.CTkLabel(
        master=frame_desc_praga,
        text="Mosca-Branca",
        font=("Poppins", 20, "bold"),
        text_color=cores['branco'],
        anchor="w"
    )
    titulo_label.place(relx=0.02, rely=0.1, anchor="w")

    desc_texto = (
        "A mosca branca é uma pequena praga agrícola que ataca diversas culturas, "
        "como tomate, feijão, algodão e ornamentais. Apesar do nome, não é uma "
        "mosca verdadeira, mas é um inseto sugador pertencente à ordem Hemiptera."
    )

    desc_label = ctk.CTkLabel(
        master=frame_desc_praga,
        text=desc_texto,
        font=("Lato", 14),
        text_color=cores['branco'],
        justify="left",
        anchor="w",
        wraplength=500
    )
    desc_label.place(relx=0.02, rely=0.35, anchor="w")

    link_label = ctk.CTkLabel(
        master=frame_desc_praga,
        text="→ Ver mais sobre",
        font=("Lato", 14, "bold"),
        text_color="orange",
        anchor="w"
    )
    link_label.place(relx=0.02, rely=0.85, anchor="w")
    
    # Barra de menu
    img_home = carregar_ctk_imagem(caminho_imgs['home_menu'], (30,30))
    img_gloss = carregar_ctk_imagem(caminho_imgs['glossario_menu'], (30,30))
    img_config = carregar_ctk_imagem(caminho_imgs['config_menu'], (30,30))
    img_dados = carregar_ctk_imagem(caminho_imgs['dados_menu'],(30,30))
    img_devs = carregar_ctk_imagem(caminho_imgs['dev_menu'], (30,30))
    
    barra_menu = ctk.CTkFrame(
        dashBoard,
        height=20,
        width=350,
        corner_radius=20,
        fg_color=cores['Fundo_Menu']
    )
    barra_menu.pack(side="bottom", pady=10, anchor="s")
    
    dash_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_home, 
        text="", 
        width=40, 
        height=40, 
        hover_color=cores['branco'], 
        fg_color="transparent")
    dash_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Glosso_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_gloss, 
        text="", 
        width=40,
        height=40, 
        fg_color="transparent", 
        hover_color=cores['branco'])
    Glosso_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Config_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_config, 
        text="", 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['branco'],
        command=lambda: Abrir_Perfil(dashBoard.destroy(), Abrir_Perfil())
        )
    Config_buttom.pack(side="left", expand=True, padx=50, pady=10)

    Dados_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_dados, 
        text="", 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['branco'],  
        command=lambda: [dashBoard.destroy(), Abrir_Grafico_Praga()])
    Dados_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Devs_buttom = ctk.CTkButton(
        barra_menu, 
        text="", 
        image=img_devs, 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['branco'], 
        command=lambda: abrir_sobre_nos(dashBoard))
    Devs_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    # Criar o frame da foto + descrição
    criar_frame_foto(dashBoard)

    # Fotos detectadas
    global fotos_detectadas
    fotos_detectadas = [
        caminho_imgs['exemplo_folha'],
        caminho_imgs['exemplo_folha2'],
        caminho_imgs['exemplo_folha3'],
    ]

    # Mostrar primeira imagem
    if fotos_detectadas:
        exibir_foto_praga(fotos_detectadas[0])
    
    dashBoard.mainloop()
    

if __name__ == "__main__":
    main()