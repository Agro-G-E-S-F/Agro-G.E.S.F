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


def criar_grafico(frame_master, meses, quantidades):
    """
    Cria um gráfico de barras dinâmico dentro de um frame.
    
    Args:
        frame_master: Frame onde o gráfico será inserido
        meses: Lista com os nomes dos meses (ex: ["Jan", "Fev", ...])
        quantidades: Lista com as quantidades correspondentes (ex: [5, 8, 4, ...])
    """
    # Limpar frame antes de criar novo gráfico
    for widget in frame_master.winfo_children():
        widget.destroy()
    
    # Criar figura do matplotlib
    fig, ax = plt.subplots(figsize=(10, 4))
    fig.patch.set_alpha(0)
    ax.set_facecolor("none")
    
    # Criar barras com gradiente visual
    barras = ax.bar(meses, quantidades, width=0.6, color=cores['Fundo_azul'], edgecolor=cores['verde_primario'], linewidth=2)
    
    # Configurar eixos
    ax.get_yaxis().set_visible(False)
    ax.spines['right'].set_visible(False)
    ax.spines['top'].set_visible(False)
    ax.spines['left'].set_visible(False)
    ax.spines['bottom'].set_color(cores['verde_primario'])
    ax.spines['bottom'].set_linewidth(2)
    ax.grid(False)
    
    # Adicionar valores no topo das barras
    for barra in barras:
        altura = barra.get_height()
        ax.text(
            barra.get_x() + barra.get_width() / 2, 
            altura + 0.5,
            str(int(altura)), 
            ha='center', 
            va='bottom', 
            fontsize=12, 
            fontweight='bold',
            color=cores['verde_primario']
        )
    
    # Configurar labels dos meses
    ax.set_xticklabels(meses, rotation=0, fontsize=11, fontweight='bold')
    ax.tick_params(axis='x', colors=cores['cor_texto'])
    
    # Remover título e labels dos eixos
    ax.set_title("")
    ax.set_xlabel("")
    ax.set_ylabel("")
    
    # Adicionar ao frame
    canvas = FigureCanvasTkAgg(fig, master=frame_master)
    canvas.draw()
    widget_canvas = canvas.get_tk_widget()
    widget_canvas.config(bg=cores['frame_bg'], highlightthickness=0)
    widget_canvas.pack(pady=20, padx=20, fill="both", expand=True)
    
    return canvas


def Abrir_Grafico_Praga(dados_meses=None, dados_quantidades=None):
    """
    Abre a janela de gráfico de detecção de pragas.
    
    Args:
        dados_meses: Lista opcional com nomes dos meses
        dados_quantidades: Lista opcional com quantidades de pragas
    """
    ctk.set_appearance_mode("light")
    ctk.set_default_color_theme("blue")
    
    from perfil import Abrir_Perfil
    
    # Dados padrão caso não sejam fornecidos
    if dados_meses is None:
        dados_meses = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"]
    if dados_quantidades is None:
        dados_quantidades = [5, 8, 4, 10, 6, 7, 9, 3, 5, 11, 6, 4]
    
    grafico_win = ctk.CTk()
    grafico_win.title("Gráfico de Detecção")
    centralizar_janela(grafico_win, 1200, 700)
    grafico_win.configure(fg_color=cores['cor_fundo'])
    grafico_win.resizable(True, True)

    if Path(caminho_imgs['icon']).is_file():
        grafico_win.iconbitmap(caminho_imgs['icon'])

    # Imagens decorativas (igual à tela de login)
    moita1_img = carregar_ctk_imagem(caminho_imgs['moita_1'], (350, 130))
    moita2_img = carregar_ctk_imagem(caminho_imgs['moita_2'], (350, 130))
    moita_flu_1 = carregar_ctk_imagem(caminho_imgs['moita_flutu'], (230, 320))
    moita_flu_2 = carregar_ctk_imagem(caminho_imgs['moita_flutu2'], (220, 290))
    
    # Inferior direita
    if moita1_img:
        moita1_label = ctk.CTkLabel(master=grafico_win, image=moita1_img, text="")
        moita1_label.place(relx=1.0, rely=1.0, anchor="se")

    # Inferior esquerda
    if moita2_img:
        moita2_label = ctk.CTkLabel(master=grafico_win, image=moita2_img, text="")
        moita2_label.place(relx=0.0, rely=1.0, anchor="sw")

    # Superior esquerda
    if moita_flu_1:
        moita_flu_1_label = ctk.CTkLabel(master=grafico_win, image=moita_flu_1, text="")
        moita_flu_1_label.place(relx=0.0, rely=0.0, anchor="nw", x=30)

    # Superior direita
    if moita_flu_2:
        moita_flu_2_label = ctk.CTkLabel(master=grafico_win, image=moita_flu_2, text="")
        moita_flu_2_label.place(relx=1.0, rely=0.0, anchor="ne", x=-30)

    # Título
    Titulo_Label = ctk.CTkLabel(
        master=grafico_win,
        text="Identificação de Pragas",
        font=("Poppins", 38, "bold"),
        text_color=cores['verde_primario'],
    )
    Titulo_Label.place(relx=0.33, rely=0.03)
    
    Desc_Titu = ctk.CTkLabel(
        master=grafico_win,
        text="Veja quantas pragas foram encontradas em cada mês",
        font=("Roboto", 18),
        text_color=cores['cor_texto']
    )
    Desc_Titu.place(relx=0.28, rely=0.10)
    
    # Frame do gráfico com sombra visual
    frame_Pragas_dados = ctk.CTkFrame(
        master=grafico_win,
        fg_color=cores['frame_bg'],
        width=1000,
        height=500,
        corner_radius=30,
        border_width=3,
        border_color=cores['verde_primario']
    )
    frame_Pragas_dados.place(relx=0.5, rely=0.52, anchor="center")
    
    # Criar gráfico dinâmico
    canvas_grafico = criar_grafico(frame_Pragas_dados, dados_meses, dados_quantidades)
    
    # Calcular total de pragas
    total_pragas = sum(dados_quantidades)
    
    # Label de informação adicional
    info_label = ctk.CTkLabel(
        master=grafico_win,
        text=f"Total de Pragas Detectadas: {total_pragas}",
        font=("Roboto", 16, "bold"),
        text_color=cores['verde_primario']
    )
    info_label.place(relx=0.5, rely=0.85, anchor="center")

    # Barra de menu
    img_home = carregar_ctk_imagem(caminho_imgs['home_menu'], (30, 30))
    img_gloss = carregar_ctk_imagem(caminho_imgs['glossario_menu'], (30, 30))
    img_config = carregar_ctk_imagem(caminho_imgs['config_menu'], (30, 30))
    img_dados = carregar_ctk_imagem(caminho_imgs['dados_menu'], (30, 30))
    img_devs = carregar_ctk_imagem(caminho_imgs['dev_menu'], (30, 30))
    
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
        hover_color=cores['verde_primario'], 
        fg_color="transparent",
        command=lambda: [grafico_win.destroy(), __import__("Dashboard").main()]
    )
    dash_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Glosso_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_gloss, 
        text="", 
        width=40,
        height=40, 
        fg_color="transparent",
        hover_color=cores['verde_primario'],
        command=lambda: [grafico_win.destroy(), __import__("Glossario").abrir_glossario()]
    )
    Glosso_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Config_buttom = ctk.CTkButton(
        barra_menu, 
        image=img_config, 
        text="", 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['verde_primario'],
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
        hover_color=cores['verde_primario']
    )
    Dados_buttom.pack(side="left", expand=True, padx=50, pady=10)
    
    Devs_buttom = ctk.CTkButton(
        barra_menu, 
        text="", 
        image=img_devs, 
        width=40, 
        height=40, 
        fg_color="transparent", 
        hover_color=cores['verde_primario'],
        command=lambda: abrir_sobre_nos(grafico_win)
    )
    Devs_buttom.pack(side="left", expand=True, padx=50, pady=10)

    grafico_win.mainloop()


# Exemplo de uso com dados customizados:
if __name__ == "__main__":
    # Dados customizados de exemplo
    meses_custom = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun"]
    pragas_custom = [12, 15, 8, 20, 18, 10]
    
    # Chamar a função com dados customizados
    Abrir_Grafico_Praga(meses_custom, pragas_custom)
    
    # Ou sem parâmetros para usar os dados padrão
    # Abrir_Grafico_Praga()