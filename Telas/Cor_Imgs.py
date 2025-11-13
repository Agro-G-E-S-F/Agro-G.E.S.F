from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

# dicionario de cores
cores = {
    'cor_fundo': "#274022",
    'frame_bg': "#FEF2D5",
    'entrada_bg': "#DADADA",
    'verde_primario': "#4C8042",
    'verde_primario_hover': "#3F6937",
    'amarelo_secundario': "#ADA339",
    'amarelo_secundario_hover': "#918930",
    'vermelho_primario': "#DB2D2D",
    'vermelho_secundario_hover':"#B12727",
    'cinza': "#808080",
    'cinza_hover': "#4a4a4a",
    'branco': "#FFFFFF",
    'preto': "#000000",
    'Fundo_Menu': "#a4c639",
    'Fundo_azul': "#4A6FA5",
    'fundo_azul_glosario': "#6B9AA4",
    'Borda_Frame': "#E0E556",
    'cor_texto': "#22532C",
    'cor_glossario_titulo': "#ADA339",
    'cor_borda_titulo_glossario': "#366C0C"
}

caminho_imgs = {
    'Enzo': BASE_DIR / "Imgs/devs/Foto_Enzo.png",
    'Felipe': BASE_DIR / "Imgs/devs/Foto_Felipe.png",
    'Gustvao': BASE_DIR / "Imgs/devs/Foto_Gustavo.png",
    'Sak': BASE_DIR / "Imgs/devs/Foto_Sakiri.png",
    'icon': BASE_DIR / "Imgs/IconeAgro.ico",
    'logo': BASE_DIR / "Imgs/LogoFinal.png",
    'conta': BASE_DIR / "Imgs/icons/Perfil.png",
    'email': BASE_DIR / "Imgs/icons/email.png",
    'senha': BASE_DIR / "Imgs/icons/SenhaLogo.png",
    'moita_1': BASE_DIR / "Imgs/visu_element/Moita_1.png",
    'moita_2': BASE_DIR / "Imgs/visu_element/Moita_2.png",
    'moita_flutu': BASE_DIR / "Imgs/visu_element/Moita_flutuante.png",
    'moita_flutu2': BASE_DIR / "Imgs/visu_element/Moita_flutuante2.png",
    'folha_dash_menu_inferior': BASE_DIR / "Imgs/visu_element/Folha_dash_inferior_direito.png",
    'folha_dash_menu_superior': BASE_DIR / "Imgs/visu_element/Folha_dash_superior_esquerdo.png",
    'Seta_dash_direita': BASE_DIR/ "Imgs/visu_element/Seta_direita.png",
    'Seta_dash_esquerda': BASE_DIR/ "Imgs/visu_element/Seta_esquerda.png",
    'Perfil_default': BASE_DIR/ "Imgs/visu_element/Teste_Img2.png",
    'Perfil_button': BASE_DIR/ "Imgs/visu_element/Alterar_perfil_button.png",
    'Logo_praga_glossario': BASE_DIR/ "Imgs/visu_element/Logo_doença.png",
    'Logo_doença_glossario': BASE_DIR/ "Imgs/visu_element/Logo_praga.png",

    #imagens de exemplo para o dashboard(tirar elas depois)
    'exemplo_folha': BASE_DIR/ "Imgs/visu_element/exemplo_folha.jpeg",
    'exemplo_folha2': BASE_DIR/ "Imgs/visu_element/exemplo_folha2.jpg",
    'exemplo_folha3': BASE_DIR/ "Imgs/visu_element/exemplo_folha3.jpg",
    

    # imagens do Menu
    'home_menu': BASE_DIR / "Imgs/visu_element/Icone_dash_menu.png",
    'glossario_menu': BASE_DIR / "Imgs/visu_element/Icone_gloss_menu.png",
    'config_menu': BASE_DIR / "Imgs/visu_element/icone_config_menu.png",
    'dev_menu': BASE_DIR / "Imgs/visu_element/devs_icon_menu.png",
    'dados_menu': BASE_DIR / "Imgs/visu_element/dados_icon_menu.png"
}