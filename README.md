# Agro-G.E.S.F: Sistema Inteligente de Monitoramento e Prevenção de Pragas em Cultivos em Linha

![Python](https://img.shields.io/badge/python-3.8+-blue.svg)
![PyTorch](https://img.shields.io/badge/PyTorch-EE4C2C?logo=pytorch&logoColor=white)
![OpenCV](https://img.shields.io/badge/OpenCV-5C3EE8?logo=opencv&logoColor=white)
![TorchVision](https://img.shields.io/badge/TorchVision-EE4C2C?logoColor=white)
![CustomTkinter](https://img.shields.io/badge/CustomTkinter-FF6B6B?logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?logo=sqlite&logoColor=white)
![Raspberry Pi](https://img.shields.io/badge/Raspberry%20Pi%205-A22846?logo=raspberrypi&logoColor=white)
![Status](https://img.shields.io/badge/status-TCC%202025-blue)

<div align="center">
  <img alt="AgroGESF Logo" src="Imagens_readme/Logo.jpg" width="600">
  
  **Sistema embarcado para detecção precoce de pragas e doenças em plantações utilizando redes neurais convolucionais**
</div>

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Pragas e Doenças Detectadas](#pragas-e-doenças-detectadas)
- [Características Principais](#características-principais)
- [Requisitos do Sistema](#requisitos-do-sistema)
- [Instalação](#instalação)
- [Tecnologias](#tecnologias)
- [Resultados Esperados](#resultados-esperados)
- [Orientação Acadêmica](#orientação-acadêmica)
- [Documentação](#documentação)
- [Equipe](#equipe)
- [Solução de Problemas](#solução-de-problemas)

## Sobre o Projeto

O Agro-G.E.S.F é um sistema integrado de hardware e software desenvolvido para auxiliar pequenos produtores rurais na identificação precoce de pragas e doenças em plantações em linha. Utiliza um carrinho motorizado com controle remoto equipado com câmera e Raspberry Pi 5 para processamento local via redes neurais convolucionais.

**Problema identificado:** Detecção tardia de pragas resulta em perdas de até 30% da produção e aplicação excessiva de agrotóxicos.

**Solução proposta:** Sistema autônomo de monitoramento com IA para identificação precoce de pragas e doenças específicas.

## Pragas e Doenças Detectadas

O sistema foi treinado para identificar:
- **Vaquinha-Verde-Amarela**: Praga comum que ataca diversas culturas
- **Requeima**: Doença fúngica devastadora em solanáceas
- **Pinta-Preta**: Doença foliar frequente em plantações

## Características Principais

### Hardware
- Carrinho motorizado com controle remoto
- Câmera Logitech Brio 100 (Full HD 1920x1080)
- Raspberry Pi 5 para processamento local
- Sistema de bateria para operação no campo

### Software
- Rede Neural Convolucional para classificação de imagens
- Interface desktop em Python com CustomTkinter
- Banco de dados SQLite para histórico
- Processamento em tempo real com OpenCV

## Requisitos do Sistema

| Componente | Mínimo | Recomendado |
|------------|--------|-------------|
| **CPU** | ARM Cortex-A76 2,4 GHz 4-core | AMD Ryzen 7 5700G / Intel i7-10700 |
| **RAM** | 8 GB LPDDR4 | 16 GB DDR4+ |
| **GPU** | VideoCore VII integrado | AMD RX 6750 XT / NVIDIA RTX 3060 |
| **Armazenamento** | 20 GB disponível | 50 GB disponível |

## Instalação

### Pré-requisitos
Sistema operacional Linux ou Windows compatível.

### Passos

1. **Download do modelo treinado**
   - [Releases v1.0.0-pre](https://github.com/GustavoS07/Agro-G.E.S.F/releases/tag/v1.0.0-pre)

2. **Clone e configuração**
   ```bash
   git clone https://github.com/GustavoS07/Agro-G.E.S.F.git
   cd Agro-G.E.S.F
   git checkout Aprendizado_Estruturado
   ```

3. **Dependências**
   ```bash
   pip install torch torchvision Pillow
   ```

4. **Configuração do modelo**
   - Mover arquivo .pth para pasta `outputs/`

5. **Execução**
   ```bash
   cd src/inference
   python3 teste.py
   ```

## Tecnologias

**Core:** Python 3.8+, PyTorch, TorchVision, OpenCV, NumPy, Pillow

**Interface:** CustomTkinter, Matplotlib

**Dados:** SQLite

**Hardware:** Raspberry Pi 5

## Resultados Esperados

### Benefícios Técnicos
- Detecção precoce de pragas e doenças
- Processamento em tempo real no campo
- Histórico detalhado das condições da lavoura
- Interface intuitiva para pequenos produtores

### Impacto Agrícola
- Redução de perdas na produção (até 30% em alguns casos)
- Diminuição do uso de agrotóxicos
- Melhoria na tomada de decisão
- Democratização da agricultura de precisão

## Orientação Acadêmica

**Orientador:** Prof. Esp. Jeferson Roberto de Lima  
**Instituição:** ETEC Zona Leste - Centro Estadual de Educação Tecnológica Paula Souza  
**Curso:** Técnico em Desenvolvimento de Sistemas AMS  
**Ano:** 2025

## Documentação

- Monografia com fundamentação teórica
- Diagramas UML (casos de uso, sequência, atividade)
- Wireframes de interface
- Especificações técnicas completas

## Equipe

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/GustavoS07">
        <img src="https://avatars.githubusercontent.com/u/133404275?v=4" width="80px;" alt="Gustavo"/><br>
        <b>Gustavo de Souza</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/EnzoCostaPaz">
        <img src="https://avatars.githubusercontent.com/u/133404019?v=4" width="80px;" alt="Enzo"/><br>
        <b>Enzo Costa</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/osakirii">
        <img src="https://avatars.githubusercontent.com/u/68735816?v=4" width="80px;" alt="Sakiri"/><br>
        <b>Sakiri Moon</b>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/lipedeoliveira">
        <img src="https://avatars.githubusercontent.com/u/129530532?v=4" width="80px;" alt="Felipe"/><br>
        <b>Felipe Vieira</b>
      </a>
    </td>
  </tr>
</table>

## Solução de Problemas

**Erro ao executar modelo:**
- Verificar se arquivo .pth está em `outputs/`
- Confirmar instalação das dependências
- Verificar compatibilidade PyTorch

**Performance baixa:**
- Confirmar requisitos mínimos de hardware
- Considerar uso de GPU se disponível

---

**TCC 2025 - ETEC Zona Leste**  
**Orientador:** Prof. Esp. Jeferson Roberto de Lima  
**Curso:** Técnico em Desenvolvimento de Sistemas
