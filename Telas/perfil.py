import customtkinter as ctk
import tkinter as tk
from tkinter import filedialog
from pathlib import Path
from PIL import Image, ImageDraw
from SobreNos import abrir_sobre_nos
from Grafico_Pragas import Abrir_Grafico_Praga
from Cor_Imgs import cores, caminho_imgs


# Funções auxilixares  

def circular_crop(img, size):
    """Recorta imagem em círculo com transparência"""
    img = img.resize(size, Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size[0], size[1]), fill=255)
    output = Image.new("RGBA", size, (0, 0, 0, 0))
    output.paste(img, (0, 0), mask)
    return output


def centralizar_janela(window, width, height):
    screen_w = window.winfo_screenwidth()
    screen_h = window.winfo_screenheight()
    x = (screen_w - width) // 2
    y = (screen_h - height) // 2
    window.geometry(f"{width}x{height}+{x}+{y}")


def carregar_ctk_imagem(path, size, apply_crop=True):
    if Path(path).is_file():
        img = Image.open(path)
        if apply_crop:
            return ctk.CTkImage(light_image=circular_crop(img, size), size=size)
        else:
            img = img.resize(size, Image.LANCZOS).convert("RGBA")
            return ctk.CTkImage(light_image=img, size=size)
    else:
        print(f"Imagem não encontrada: {path}")
        return None


def add_placeholder(entry, placeholder_text, color_placeholder=cores['cor_texto'], color_text=cores['cor_texto']):
    """Adiciona placeholder a um CTkEntry"""
    def on_focus_in(event):
        if entry.get() == placeholder_text:
            entry.delete(0, tk.END)
            entry.configure(text_color=color_text, show="")  # mostra o texto normal ao focar

    def on_focus_out(event):
        if entry.get() == "":
            entry.insert(0, placeholder_text)
            entry.configure(text_color=color_placeholder)
            # Se for senha, placeholder deve aparecer em texto normal
            if hasattr(entry, "_is_password") and entry._is_password:
                entry.configure(show="")

    entry.insert(0, placeholder_text)
    entry.configure(text_color=color_placeholder)

    entry.bind("<FocusIn>", on_focus_in)
    entry.bind("<FocusOut>", on_focus_out)
    
    
    
#janela de perfil 

def Abrir_Perfil(grafico_win=None):
    ctk.set_appearance_mode("light")
    ctk.set_default_color_theme("blue")

    Janela_Pefil_win = ctk.CTk()
    Janela_Pefil_win.title("Página de Perfil")
    centralizar_janela(Janela_Pefil_win, 1200, 700)
    Janela_Pefil_win.configure(fg_color=cores['cor_fundo'])
    Janela_Pefil_win.resizable(True, True)

    # ícone (se existir)
    if Path(caminho_imgs.get('icon', '')).is_file():
        try:
            Janela_Pefil_win.iconbitmap(caminho_imgs['icon'])
        except Exception as e:
            print(f"Não foi possível definir o ícone: {e}")

    # carregar imagens
    Perfil_Default = carregar_ctk_imagem(caminho_imgs.get('Perfil_default'), (280, 280))
    Botão_trocar_img = carregar_ctk_imagem(caminho_imgs.get('Perfil_button'), (100, 100))

    # título
    Titulo = ctk.CTkLabel(
        master=Janela_Pefil_win,
        text="Perfil",
        font=("Poppins", 30, "bold"),
        text_color=cores['branco'],
    )
    Titulo.place(relx=0.47, rely=0.12)

    # frame dos dados
    Menu_Dados = ctk.CTkFrame(
        master=Janela_Pefil_win,
        width=640,
        height=380,
        fg_color=cores['frame_bg'],
        corner_radius=50
    )
    Menu_Dados.place(relx=0.43, rely=0.2)

    # entradas
    Entrada_Nome = ctk.CTkEntry(
        master=Menu_Dados,
        width=520,
        height=40,
        corner_radius=20,
        text_color=cores['cor_texto'],
        border_width=0,
        fg_color=cores['entrada_bg'],
        font=("Roboto", 15)
    )
    Entrada_Nome.place(relx=0.1, rely=0.1)
    add_placeholder(Entrada_Nome, "Nome do Usuário", cores['cor_texto'], cores['cor_texto'])


    Entrada_Data = ctk.CTkEntry(
        master=Menu_Dados,
        width=150,
        height=40,
        corner_radius=20,
        text_color=cores['cor_texto'],
        border_width=0,
        fg_color=cores['entrada_bg'],
        font=("Roboto", 15)
    )
    Entrada_Data.place(relx=0.1, rely=0.25)
    add_placeholder(Entrada_Data, "DD/MM/AAAA", cores['cor_texto'], cores['cor_texto'])

    Entrada_Email = ctk.CTkEntry(
        master=Menu_Dados,
        width=540,
        height=40,
        corner_radius=20,
        text_color=cores['cor_texto'],
        border_width=0,
        fg_color=cores['entrada_bg'],
        font=("Roboto", 15)
    )
    Entrada_Email.place(relx=0.1, rely=0.40)
    add_placeholder(Entrada_Email, "Email", cores['cor_texto'], cores['cor_texto'])

    Senha_Entrada = ctk.CTkEntry(
        master=Menu_Dados,
        width=540,
        height=40,
        corner_radius=20,
        text_color=cores['cor_texto'],
        border_width=0,
        fg_color=cores['entrada_bg'],
        font=("Roboto", 15),
        show="*"
    )
    Senha_Entrada.place(relx=0.1, rely=0.55)
    Senha_Entrada._is_password = True  # marca que é campo de senha
    add_placeholder(Senha_Entrada, "Senha", cores['cor_texto'], cores['cor_texto'])
    
    #função de checkbox marcado unico 
    def toggle_masc():
        if checkbox_masc.get() == 1:  # se marcado
            checkbox_femin.deselect()

    def toggle_femin():
        if checkbox_femin.get() == 1:
            checkbox_masc.deselect()
    
    checkbox_masc = ctk.CTkCheckBox(
        master=Menu_Dados,
        width=28,
        height=28,
        corner_radius=20,
        text="Masculino",
        border_width=2,
        fg_color= cores['amarelo_secundario'],
        hover_color=cores['amarelo_secundario_hover'],
        border_color=cores['verde_primario'],
        text_color=cores['cor_texto'],
        font=("Roboto", 15),
        command=toggle_masc
    )
    checkbox_masc.place(relx=0.4,rely=0.26)
    
    checkbox_femin = ctk.CTkCheckBox(
        master=Menu_Dados,
        width=28,
        height=28,
        corner_radius=20,
        text="feminino",
        border_width=2,
        fg_color= cores['amarelo_secundario'],
        hover_color=cores['amarelo_secundario_hover'],
        border_color=cores['verde_primario'],
        text_color=cores['cor_texto'],
        font=("Roboto", 15),
        command=toggle_femin
    )
    checkbox_femin.place(relx=0.6,rely=0.26)
    
    

    
    Editar_button = ctk.CTkButton(
        master=Menu_Dados,
        width=150,
        height=40,
        fg_color=cores['verde_primario'],
        hover_color=cores['verde_primario_hover'],
        text="Editar",
        font=("Lato", 18, "bold"),
        corner_radius=16
    )
    Editar_button.place(relx= 0.4, rely= 0.80)

    # foto de perfil
    Foto_Perfil = ctk.CTkLabel(
        master=Janela_Pefil_win,
        text="",
        width=280,
        height=280,
        corner_radius=75,
        fg_color="transparent",
        image=Perfil_Default,
        font=("Roboto", 20)
    )
    Foto_Perfil.place(relx=0.026, rely=0.25)

    # manter referência
    Janela_Pefil_win._perfil_img_ref = Perfil_Default
    Foto_Perfil._img_ref = Perfil_Default

    # função para escolher nova foto
    def escolher_nova_foto():
        caminho = filedialog.askopenfilename(
            filetypes=[("Imagens", "*.png;*.jpg;*.jpeg;*.gif")]
        )
        if caminho:
            nova_img = carregar_ctk_imagem(caminho, (260, 260))
            if nova_img:
                Foto_Perfil.configure(image=nova_img)
                Foto_Perfil._img_ref = nova_img
                Janela_Pefil_win._perfil_img_ref = nova_img
                print("Nova foto de perfil escolhida:", caminho)

    # botão para trocar foto
    Botao_Trocar_Foto = ctk.CTkButton(
        master=Janela_Pefil_win,
        text="",
        image=Botão_trocar_img,
        width=100,
        height=100,
        fg_color="transparent",
        command=escolher_nova_foto
    )
    Botao_Trocar_Foto.place(relx=0.01, rely=0.55)
    Botao_Trocar_Foto.configure(hover=False)

    # Menu 
    img_home = carregar_ctk_imagem(caminho_imgs['home_menu'], (30, 30),apply_crop=False)
    img_gloss = carregar_ctk_imagem(caminho_imgs['glossario_menu'], (30, 30),apply_crop=False)
    img_config = carregar_ctk_imagem(caminho_imgs['config_menu'], (30, 30),apply_crop=False)
    img_dados = carregar_ctk_imagem(caminho_imgs['dados_menu'], (30, 30),apply_crop=False)
    img_devs = carregar_ctk_imagem(caminho_imgs['dev_menu'], (30, 30),apply_crop=False)

    barra_menu = ctk.CTkFrame(
        Janela_Pefil_win,
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
        command=lambda: [Janela_Pefil_win.destroy(), __import__("Dashboard").main()]
        )
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
        hover_color=cores['branco'])
    Config_buttom.pack(side="left", expand=True, padx=50, pady=10)

    Dados_buttom = ctk.CTkButton(
        barra_menu,
        image=img_dados,
        text="",
        width=40,
        height=40,
        fg_color="transparent",
        hover_color=cores['branco'],
        command=lambda: [Janela_Pefil_win.destroy(), Abrir_Grafico_Praga()])
    Dados_buttom.pack(side="left", expand=True, padx=50, pady=10)

    Devs_buttom = ctk.CTkButton(
        barra_menu,
        text="",
        image=img_devs,
        width=40,
        height=40,
        fg_color="transparent",
        hover_color=cores['branco'],
        command=lambda: abrir_sobre_nos(Janela_Pefil_win))
    Devs_buttom.pack(side="left", expand=True, padx=50, pady=10)

    Janela_Pefil_win.mainloop()


if __name__ == "__main__":
    Abrir_Perfil()
