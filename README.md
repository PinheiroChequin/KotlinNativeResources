# Aplicativo Kotlin com Formulário, Câmera, SQLite e Localização GPS
Este projeto consiste em um aplicativo Android desenvolvido em Kotlin que permite ao usuário preencher um formulário, tirar uma foto usando a câmera do celular e salvar os dados em um banco de dados SQLite local. Além disso, o aplicativo exibe a localização atual do usuário utilizando o GPS do dispositivo.

## Funcionalidades
- Formulário:
Campos de texto para Nome, Email e Comentário.

- Funcionalidade de Câmera:
Um botão que abre a câmera do celular para o usuário tirar uma foto.
A foto capturada é exibida no aplicativo após ser tirada.

- Armazenamento em SQLite:
Os dados preenchidos no formulário (Nome, Email, Comentário, Caminho da Foto) são salvos em um banco de dados SQLite.
O gerenciamento do banco de dados é feito utilizando SQLiteOpenHelper.

- Localização GPS:
Um botão que obtém e exibe a latitude e longitude atuais do usuário utilizando o GPS do dispositivo.
