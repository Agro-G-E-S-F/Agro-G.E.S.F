import os
import sys
current_dir = os.path.dirname(__file__)
parent_dir = os.path.abspath(os.path.join(current_dir, '..'))
sys.path.append(parent_dir)
from Data.database import cadastrarUsuario,tentarLogin,createTables,delete

delete()
createTables()

nome = "Teste"
email = "teste2"
senha =  "teste3"
rep_senha = "teste4"
imagemPerfil = "teste5"
cadastrarUsuario(nome,email,senha,rep_senha,imagemPerfil)

tentarLogin(nome,senha)