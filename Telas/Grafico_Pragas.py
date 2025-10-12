import customtkinter as ctk
import tkinter as tk
from pathlib import Path
from PIL import Image, ImageDraw
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from SobreNos import abrir_sobre_nos
from Cor_Imgs import cores, caminho_imgs

def centralizar_janela(window, width, height):
    screen_w = window.winfo_screenwidth()
    screen_h = window.winfo_screenheight()
    x = (screen_w - width) // 2
    y = (screen_h - height) // 2
    window.geometry(f"{width}x{height}+{x}+{y}")
    

def carregar_ctk_imagem(path, size):
    if Path(path).is_file():
        img = Image.open(path)
        return ctk.CTkImage(light_image=img, size=size)
    else:
        print(f"Imagem não encontrada: {path}")
        return None



def Abrir_Grafico_Praga():
    ctk.set_appearance_mode("light")
    ctk.set_default_color_theme("blue")
    
    from perfil import Abrir_Perfil  # import Local 

    
    grafico_win = ctk.CTk()
    grafico_win.title("Gráfico de Detecção")
    centralizar_janela(grafico_win, 1200, 700)
    grafico_win.configure(fg_color=cores['cor_fundo'])
    grafico_win.resizable(True, True)

    if Path(caminho_imgs['icon']).is_file():
        grafico_win.iconbitmap(caminho_imgs['icon'])

    # Título
    Titulo_Label = ctk.CTkLabel(
        master=grafico_win,
        text="Identificação de Pragas",
        font=("Poppins", 38, "bold"),
        text_color=cores['branco'],
    )
    Titulo_Label.place(relx= 0.33, rely=0.03)
    
    Desc_Titu = ctk.CTkLabel(
        master=grafico_win,
        text="Veja quantas pragas foram encontradas em cada mês",
        font=("Roboto", 20),
        text_color=cores['branco']
    )
    Desc_Titu.place(relx= 0.31, rely=0.10)
    
    frame_Pragas_dados = ctk.CTkFrame(
        master=grafico_win,
        fg_color=cores['frame_bg'],
        width=920,
        height=470,
        corner_radius=30
    )
    frame_Pragas_dados.place(relx=0.5, rely=0.5, anchor="center")
    
    # grafico
    meses = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"]
    quantidades = [5, 8, 4, 10, 6, 7, 9, 3, 5, 11, 6, 4]
    
    fig, ax = plt.subplots(figsize=(10,4))
    fig.patch.set_alpha(0)       # Fundo da figura
    ax.set_facecolor("none")     # Fundo dos eixos
    barras = ax.bar(meses, quantidades, width=0.6, color=cores['Fundo_azul'])
    ax.get_yaxis().set_visible(False)
    
    ax.spines['right'].set_visible(False)
    ax.spines['top'].set_visible(False)
    ax.spines['left'].set_visible(False)
    ax.grid(False)
    
    for barra in barras:
        altura = barra.get_height()
        ax.text(barra.get_x() + barra.get_width() / 2, altura + 0.5,
            str(altura), ha='center', va='bottom', fontsize=10, color='black')
        
    ax.set_xticklabels(meses, rotation=0)
    
    # Remover título e labels dos eixos se for necessário (deixa só o visual das barras)
    ax.set_title("")
    ax.set_xlabel("")
    ax.set_ylabel("")
    
    canvas = FigureCanvasTkAgg(
        fig,
        master=frame_Pragas_dados,
    )
    canvas.draw()
    canvas.get_tk_widget()
    widget_canvas = canvas.get_tk_widget()
    widget_canvas.config(bg=cores['frame_bg'], highlightthickness=0)  # remove borda e define fundo
    widget_canvas.pack(pady=20)

    

    # Barra de menu
    img_home = carregar_ctk_imagem(caminho_imgs['home_menu'], (30,30))
    img_gloss = carregar_ctk_imagem(caminho_imgs['glossario_menu'], (30,30))
    img_config = carregar_ctk_imagem(caminho_imgs['config_menu'], (30,30))
    img_dados = carregar_ctk_imagem(caminho_imgs['dados_menu'],(30,30))
    img_devs = carregar_ctk_imagem(caminho_imgs['dev_menu'], (30,30))
    
    barra_menu = ctk.CTkFrame(
        grafico_win,
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
    command=lambda: [grafico_win.destroy(), __import__("Dashboard").main()] #faz o import por aqui
    )
    dash_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Glosso_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_gloss, 
        text="", width=40,
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
        command=lambda: Abrir_Perfil(grafico_win.destroy(), Abrir_Perfil())
        )
    Config_buttom.pack(side="left", expand=True, padx=50, pady=10)

    Dados_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_dados, 
        text="", 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['branco'])
    Dados_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Devs_buttom = ctk.CTkButton(
        barra_menu, 
        text="", 
        image=img_devs, 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['branco'],
        command=lambda: abrir_sobre_nos(grafico_win))
    Devs_buttom.pack(side="left", expand=True, padx=50, pady=10)
    

    grafico_win.mainloop()
