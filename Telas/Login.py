from pathlib import Path
from PIL import Image
import customtkinter as ctk
from Cor_Imgs import cores, caminho_imgs
import Dashboard
from Data.database import  cadastrarUsuario, tentarLogin,createTables


# Funções Utilitárias

# centrailizar janelas 
def centralizar_janela(window, width, height):
    screen_w = window.winfo_screenwidth()
    screen_h = window.winfo_screenheight()
    x = (screen_w - width) // 2
    y = (screen_h - height) // 2
    window.geometry(f"{width}x{height}+{x}+{y}")

# faz uma pré-carregamento das imagens 
def carregar_ctk_imagem(path, size):
    if Path(path).is_file():
        img = Image.open(path)
        return ctk.CTkImage(light_image=img, size=size)
    return None

# cria um campo com imagem 
def criar_campo_com_imagem(master, text, icon_path, size, show=None):
    icon_img = carregar_ctk_imagem(icon_path, size) if icon_path else None
    label = ctk.CTkLabel(
        master, 
        text=text, 
        image=icon_img, 
        compound="right", 
        font=("Lato", 15, "bold")
        )
    label.pack(padx=60, pady=(5, 2), anchor="w")

    entrada = ctk.CTkEntry(
        master=master,
        width=350,
        height=40,
        fg_color=cores['entrada_bg'],
        border_width=0,
        corner_radius=10,
        text_color=cores['cor_texto'],
        show=show
    )
    entrada.pack(padx=60, pady=(0, 5), anchor="w")
    return entrada

    
# Interface 
def main():
    try:
        createTables()
    except:
        pass # ---> Caso a tabela já exista
    ctk.set_appearance_mode("light")
    ctk.set_default_color_theme("blue")

    janela = ctk.CTk()
    janela.title("AGRO G.E.S.F")
    centralizar_janela(janela, 1200, 700)
    janela.configure(fg_color=cores['cor_fundo'])
    
    
    def abrir_dashboard():
        nome = entrada_nome_login.get()
        senha = entrada_senha_login;get()

        if not nome or not senha:
            #Trocar por ctk
            print("Preencha todos os campos")
            return
        
        def sucesso():
            janela.destroy() 
            Dashboard.main()   # abre dashboard
    tentarLogin(nome,senha,on_sucess=sucesso)
    if Path(caminho_imgs['icon']).is_file():janela.iconbitmap(caminho_imgs['icon'])
    def realizar_cadastro():
        nome = entrada_nome_cad.get()
        email = entrada_email_cad.get()
        senha = entrada_senha_cad.get()
        rep_senha = entrada_nep_senha.get()
    
        if not nome or not email or not senha or not rep_senha:
            print("Preencha todos os campos") #---> Colocar em CTK
            return
        if senha != rep_senha:
            print("As senhas não coincidem") #---> Colocar em CTK
            return
        
        cadastrarUsuario(nome,email,senha,rep_senha,"")
        print("Cadastro realizado com sucesso") #---> Colocar em CTK
        mostrar_login()

    # Cria o frame basico de Login
    frame_login = ctk.CTkFrame(
        janela, 
        width=721, 
        height=789, 
        corner_radius=20, 
        fg_color=cores['frame_bg'])
    frame_login.place(relx=0.5, rely=0.5, anchor="center")
    
    # Imagens Visuais
    moita1_img = carregar_ctk_imagem(caminho_imgs['moita_1'], (350, 130))
    moita2_img = carregar_ctk_imagem(caminho_imgs['moita_2'], (350, 130))
    moita_flu_1 = carregar_ctk_imagem(caminho_imgs['moita_flutu'], (230, 320))
    moita_flu_2 = carregar_ctk_imagem(caminho_imgs['moita_flutu2'], (220, 290))
    
    # inferior direita
    if moita1_img:
        moita1_label = ctk.CTkLabel(
            master=janela, 
            image=moita1_img, 
            text="")
        moita1_label.place(relx=1.0, rely=1.0, anchor="se")

    # inferior esquerda
    if moita2_img:
        moita2_label = ctk.CTkLabel(
            master=janela, 
            image=moita2_img, 
            text="")
        moita2_label.place(relx=0.0, rely=1.0, anchor="sw")

    # superior esquerda
    if moita_flu_1:
        moita_flu_1_label = ctk.CTkLabel(
            master=janela, 
            image=moita_flu_1, 
            text="")
        moita_flu_1_label.place(relx=0.0, rely=0.0, anchor="nw", x=30)

    # superior direita
    if moita_flu_2:
        moita_flu_2_label = ctk.CTkLabel(
            master=janela, 
            image=moita_flu_2, 
            text="")
        moita_flu_2_label.place(relx=1.0, rely=0.0, anchor="ne", x=-30)

    # cria o frame basico do Cadastro
    frame_cadastro = ctk.CTkFrame(
        janela, 
        width=721, 
        height=789, 
        corner_radius=20,
        fg_color=cores['frame_bg'])

    # Funções de troca de frame
    def mostrar_login():
        frame_cadastro.place_forget() # <-- place_forget() esconde a tela fazendo com que a outra janela seja exibida
        frame_login.place(relx=0.5, rely=0.5, anchor="center")

    def mostrar_cadastro():
        frame_login.place_forget()
        frame_cadastro.place(relx=0.5, rely=0.5, anchor="center")

    #Conteúdo Login
    logo_img = carregar_ctk_imagem(caminho_imgs['logo'], (300, 160))
    if logo_img:
        ctk.CTkLabel(
            frame_login, 
            image=logo_img, 
            text="").pack(pady=(5, 10))

    entrada_nome_login = criar_campo_com_imagem(frame_login, "Nome   ", caminho_imgs['conta'], (35, 35))
    entrada_senha_login = criar_campo_com_imagem(frame_login, "Senha   ", caminho_imgs['senha'], (35, 35), show="*")

    Login_Buttom = ctk.CTkButton(
        frame_login, 
        text="ENTRAR", 
        width=230, 
        height=60,
        corner_radius=10,
        font=("Lato", 17, "bold"),
        fg_color=cores['verde_primario'],
        hover_color=cores['verde_primario_hover'],
        command=abrir_dashboard
    ).pack(pady=(10, 20))


    Criar_Buttom= ctk.CTkButton(
        frame_login, 
        text="CRIAR UMA CONTA",
        corner_radius=10,
        width=190, 
        height=30,
        fg_color=cores['amarelo_secundario'],
        hover_color=cores['amarelo_secundario_hover'],
        command=mostrar_cadastro
    ).pack(pady=(20, 30))
    
    sem_conta_label = ctk.CTkLabel(
        master=frame_login, 
        text="Não tem uma conta?", 
        font=("Lato", 15))
    sem_conta_label.place(x=170, y=425)



    # Conteúdo Cadastro
    logo_img2 = carregar_ctk_imagem(caminho_imgs['logo'], (300, 160))
    if logo_img2:
        ctk.CTkLabel(
            frame_cadastro, 
            image=logo_img2, 
            text="").pack(pady=(5, 10))

    entrada_nome_cad = criar_campo_com_imagem(frame_cadastro, "Nome   ", caminho_imgs['conta'], (35, 35))
    entrada_email_cad = criar_campo_com_imagem(frame_cadastro, "Email   ", caminho_imgs['email'], (35, 35))
    entrada_senha_cad = criar_campo_com_imagem(frame_cadastro, "Senha   ", caminho_imgs['senha'], (35, 35), show="*")
    entrada_rep_senha_cad = criar_campo_com_imagem(frame_cadastro, "Repetir Senha   ", None, (0, 0), show="*")

    Cadastra_Buttom = ctk.CTkButton(
        frame_cadastro, text="CADASTRAR", 
        width=170, 
        height=40,
        font=("Lato", 15, "bold"),
        fg_color=cores['verde_primario'],
        corner_radius=10,
        hover_color=cores['verde_primario_hover'],
        command=realizar_cadastro
    ).pack(pady=(10, 10))

    Voltar_Buttom = ctk.CTkButton(
        frame_cadastro, 
        text="VOLTAR", 
        width=130, 
        height=35,
        corner_radius=20,
        fg_color=cores['amarelo_secundario'],
        hover_color=cores['amarelo_secundario_hover'],
        command=mostrar_login
    ).pack(pady=(10, 20))

    janela.mainloop()

if __name__ == "__main__":
    main()
