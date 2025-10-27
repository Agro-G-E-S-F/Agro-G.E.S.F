import customtkinter as ctk
from PIL import Image, ImageDraw
from pathlib import Path
from Cor_Imgs import cores, caminho_imgs
from SobreNos import abrir_sobre_nos

def centralizar_janela(window, width, height):
    """Centraliza a janela na tela"""
    screen_w = window.winfo_screenwidth()
    screen_h = window.winfo_screenheight()
    x = (screen_w - width) // 2
    y = (screen_h - height) // 2
    window.geometry(f"{width}x{height}+{x}+{y}")


def carregar_ctk_imagem(path, size):
    """Carrega imagem redimensionada se o caminho for válido"""
    if Path(path).is_file():
        img = Image.open(path)
        return ctk.CTkImage(light_image=img, size=size)
    return None


def imagem_circular(path, size=(80, 80)):
    """Cria uma imagem circular com transparência ou placeholder"""
    if not path or not Path(path).is_file():
        # cria um círculo cinza placeholder se não houver imagem válida
        img = Image.new("RGBA", size, (220, 220, 220, 255))
        draw = ImageDraw.Draw(img)
        draw.ellipse((0, 0, size[0], size[1]), fill=(150, 150, 150, 255))
        return ctk.CTkImage(light_image=img, size=size)

    img = Image.open(path).resize(size)
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size[0], size[1]), fill=255)
    img.putalpha(mask)
    return ctk.CTkImage(light_image=img, size=size)


# ------------------------
# Tela principal
# ------------------------

def abrir_glossario():
    ctk.set_appearance_mode("light")
    ctk.set_default_color_theme("blue")

    from perfil import Abrir_Perfil  # import local

    Tela_Glossario = ctk.CTk()
    Tela_Glossario.title("Glossário de Pragas")
    centralizar_janela(Tela_Glossario, 1200, 700)
    Tela_Glossario.configure(fg_color=cores['cor_fundo'])

    if Path(caminho_imgs['icon']).is_file():
        Tela_Glossario.iconbitmap(caminho_imgs['icon'])

    # ------------------------
    # Elementos visuais decorativos
    # ------------------------
    Folha_inferior = carregar_ctk_imagem(caminho_imgs['folha_dash_menu_inferior'], (220, 220))
    Folha_superior = carregar_ctk_imagem(caminho_imgs['folha_dash_menu_superior'], (220, 220))
    Icon_Praga = carregar_ctk_imagem(caminho_imgs['Logo_praga_glossario'], (40, 40))
    Icon_doenca = carregar_ctk_imagem(caminho_imgs['Logo_doença_glossario'], (40, 40))

    if Folha_inferior:
        ctk.CTkLabel(
            Tela_Glossario, 
            image=Folha_inferior, 
            text="").place(relx=1.0, rely=1.0, anchor="se")
        
    if Folha_superior:
        ctk.CTkLabel(
            Tela_Glossario, 
            image=Folha_superior, 
            text="").place(relx=0.0, rely=0.0, anchor="nw")

    # ------------------------
    # Títulos
    # ------------------------
    Frame_Titulo_Praga = ctk.CTkFrame(
        master=Tela_Glossario,
        width=240, height=60,
        corner_radius=20,
        fg_color=cores['cor_glossario_titulo'],
        border_color=cores['cor_borda_titulo_glossario']
    )
    Frame_Titulo_Praga.place(relx=0.38, rely=0.15, anchor="e")

    ctk.CTkLabel(
        master=Frame_Titulo_Praga,
        image=Icon_Praga,
        compound="left",
        text=" Pragas",
        font=("Lato", 28),
        text_color=cores['branco']
    ).place(relx=0.15, rely=0.16)

    Frame_Titulo_Doenca = ctk.CTkFrame(
        master=Tela_Glossario,
        width=240, height=60,
        corner_radius=20,
        fg_color=cores['cor_glossario_titulo'],
        border_color=cores['cor_borda_titulo_glossario']
    )
    Frame_Titulo_Doenca.place(relx=0.60, rely=0.15, anchor="w")

    ctk.CTkLabel(
        master=Frame_Titulo_Doenca,
        image=Icon_doenca,
        compound="left",
        text=" Doenças",
        font=("Lato", 28),
        text_color=cores['branco']
    ).place(relx=0.15, rely=0.16)

    # Frames roláveis (azuis)
    frame_foto_pragas = ctk.CTkScrollableFrame(
        master=Tela_Glossario,
        width=450,
        height=350,
        fg_color=cores['fundo_azul_glosario'],
        corner_radius=20,
    )
    frame_foto_pragas.place(relx=0.28, rely=0.55, anchor="center")

    frame_foto_doenca = ctk.CTkScrollableFrame(
        master=Tela_Glossario,
        width=450,
        height=350,
        fg_color=cores['fundo_azul_glosario'],
        corner_radius=20,
    )
    frame_foto_doenca.place(relx=0.72, rely=0.55, anchor="center")

    # Conteúdo (pragas e doenças usando a imagem "Enzo") -- trocar pra buscar as imagens no banco de dados dps
    imagem_padrao = caminho_imgs.get("Enzo")  # pega a imagem Enzo do dicionário

    pragas = [
        "Mosca-Branca", "Lagarta-da-soja", "Caracóis", "Gafanhoto", "Percevejo"
    ]

    for i, nome in enumerate(pragas):
        img = imagem_circular(imagem_padrao)
        lbl_img = ctk.CTkLabel(frame_foto_pragas, image=img, text="")
        lbl_img.grid(row=i // 3 * 2, column=i % 3, padx=25, pady=(15, 5))
        ctk.CTkLabel(frame_foto_pragas, text=nome, font=("Lato", 14)).grid(
            row=i // 3 * 2 + 1, column=i % 3, padx=25, pady=(0, 20)
        )

    doencas = ["Título 1", "Título 2", "Título 3", "Título 4", "Título 5", "Título 6"]

    for i, nome in enumerate(doencas):
        img = imagem_circular(imagem_padrao)
        lbl_img = ctk.CTkLabel(frame_foto_doenca, image=img, text="")
        lbl_img.grid(row=i // 3 * 2, column=i % 3, padx=25, pady=(15, 5))
        ctk.CTkLabel(frame_foto_doenca, text=nome, font=("Lato", 14)).grid(
            row=i // 3 * 2 + 1, column=i % 3, padx=25, pady=(0, 20)
        )

    
    # Barra de menu
    img_home = carregar_ctk_imagem(caminho_imgs['home_menu'], (30,30))
    img_gloss = carregar_ctk_imagem(caminho_imgs['glossario_menu'], (30,30))
    img_config = carregar_ctk_imagem(caminho_imgs['config_menu'], (30,30))
    img_dados = carregar_ctk_imagem(caminho_imgs['dados_menu'],(30,30))
    img_devs = carregar_ctk_imagem(caminho_imgs['dev_menu'], (30,30))
    
    barra_menu = ctk.CTkFrame(
        Tela_Glossario,
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
    fg_color="transparent",
    command=lambda: [Tela_Glossario.destroy(), __import__("Dashboard").main()] #faz o import por aqui
    )
    dash_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Glosso_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_gloss, 
        text="", width=40,
        height=40, 
        fg_color="transparent",
        hover_color=cores['branco']
        )
    Glosso_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Config_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_config, 
        text="", 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['branco'],
        command=lambda: [Tela_Glossario.destroy(), __import__("perfil").Abrir_Perfil()]
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
        command=lambda: [Tela_Glossario.destroy(), __import__("Grafico_Pragas").Abrir_Grafico_Praga()]
        
        )
    Dados_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Devs_buttom = ctk.CTkButton(
        barra_menu, 
        text="", 
        image=img_devs, 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['branco'],
        command=lambda: abrir_sobre_nos(Tela_Glossario))
    Devs_buttom.pack(side="left", expand=True, padx=50, pady=10)

    Tela_Glossario.mainloop()


# Executar
if __name__ == "__main__":
    abrir_glossario()
