"""
Script para baixar datasets agrícolas usando AgML
Culturas: Soja, Milho, Alface, Feijão e similares
"""

import agml

# Instalação (execute no terminal primeiro):
# pip install agml

def listar_datasets_disponiveis():
    """Lista todos os datasets disponíveis no AgML"""
    print("=" * 60)
    print("DATASETS DISPONÍVEIS NO AGML")
    print("=" * 60)

    datasets = agml.data.public_data_sources()

    # Mostrar total
    print(f"\nTotal de datasets: {len(datasets)}")

    # Filtrar EXCLUINDO doenças
    print("\n### DATASETS SEM FOCO EM DOENÇAS ###\n")
    datasets_normais = []

    for ds in datasets:
        # Obter o nome do dataset (pode ser string ou objeto)
        nome = str(ds) if not isinstance(ds, str) else ds
        nome_lower = nome.lower()

        # Excluir datasets de doenças
        if 'disease' not in nome_lower and 'pest' not in nome_lower:
            # Filtrar por culturas de interesse
            culturas = ['soy', 'corn', 'maize', 'lettuce', 'bean', 'crop',
                       'weed', 'segmentation', 'detection', 'classification']
            if any(cultura in nome_lower for cultura in culturas):
                datasets_normais.append(nome)
                print(f"✓ {nome}")

    print(f"\nTotal de datasets normais encontrados: {len(datasets_normais)}")
    print("=" * 60)
    return datasets


def baixar_dataset(nome_dataset, diretorio_destino='./datasets'):
    """
    Baixa um dataset específico do AgML

    Args:
        nome_dataset: Nome do dataset (ex: 'bean_disease_uganda')
        diretorio_destino: Pasta onde salvar os dados
    """
    print(f"\nBaixando dataset: {nome_dataset}")
    print(f"Destino: {diretorio_destino}")
    print("-" * 60)

    try:
        # Carregar o dataset
        loader = agml.data.AgMLDataLoader(nome_dataset)

        # Informações do dataset
        print(f"Total de imagens: {len(loader)}")
        print(f"Classes: {loader.classes if hasattr(loader, 'classes') else 'N/A'}")
        print(f"Tipo de tarefa: {loader.info.tasks if hasattr(loader, 'info') else 'N/A'}")

        # Baixar os dados
        loader.export_to_disk(diretorio_destino)

        print(f"✓ Dataset baixado com sucesso em: {diretorio_destino}")
        return loader

    except Exception as e:
        print(f"✗ Erro ao baixar dataset: {e}")
        return None


def exemplo_uso_completo():
    """Exemplo completo de uso"""

    # 1. Listar datasets disponíveis
    print("\n### PASSO 1: Listar Datasets ###")
    datasets = listar_datasets_disponiveis()

    # 2. Datasets recomendados para suas culturas
    print("\n### PASSO 2: Datasets Recomendados ###")
    print("\nDatasets sugeridos para download:\n")

    datasets_sugeridos = {
        'Soja (Normal)': [
            'soybean_weed_uav_brazil',  # Nome correto!
            'soybean_growth_stages',
            'crop_segmentation'
        ],
        'Milho (Normal)': [
            'plant_seedlings_aarhus',  # Nome correto (inclui milho)
            'maize_growth_stages',
            'crop_weeds'
        ],
        'Alface e Vegetais': [
            'lettuce_instance_segmentation',
            'vegetable_weeds',
            'plant_seedlings_aarhus'
        ],
        'Feijão (Normal)': [
            'bean_growth_stages',
            'legume_classification'
        ],
        'Detecção de Objetos': [
            'grape_detection_californiaday',  # Funciona!
            'fruit_detection',
            'apple_detection_spain'
        ],
        'Plantas Daninhas': [
            'crop_weeds',
            'weed_detection_cropandweed',
            'early_crop_weed_segmentation'
        ]
    }

    for cultura, datasets in datasets_sugeridos.items():
        print(f"\n{cultura}:")
        for ds in datasets:
            print(f"  - {ds}")

    # 3. Exemplo de download (descomente para usar)
    print("\n### PASSO 3: Exemplo de Download ###")
    print("\nPara baixar um dataset, descomente as linhas abaixo:\n")
    print("# loader = baixar_dataset('bean_disease_uganda', './datasets/feijao')")
    print("# loader = baixar_dataset('corn_disease_makerere', './datasets/milho')")


def carregar_e_explorar_dataset(nome_dataset):
    """
    Carrega um dataset e mostra informações detalhadas
    """
    print(f"\n### Explorando Dataset: {nome_dataset} ###\n")

    try:
        loader = agml.data.AgMLDataLoader(nome_dataset)

        # Informações gerais
        print(f"Nome: {nome_dataset}")
        print(f"Total de amostras: {len(loader)}")

        if hasattr(loader, 'info'):
            info = loader.info
            print(f"Descrição: {info.description if hasattr(info, 'description') else 'N/A'}")
            print(f"Tarefas: {info.tasks if hasattr(info, 'tasks') else 'N/A'}")

        if hasattr(loader, 'classes'):
            print(f"Classes: {loader.classes}")

        # Exemplo de uma amostra
        print("\n--- Primeira amostra ---")
        img, label = loader[0]
        print(f"Shape da imagem: {img.shape if hasattr(img, 'shape') else type(img)}")
        print(f"Label: {label}")

        return loader

    except Exception as e:
        print(f"Erro: {e}")
        return None


# Executar exemplo
if __name__ == "__main__":
    print("=" * 60)
    print("AGML - DOWNLOAD DE DATASETS AGRÍCOLAS")
    print("=" * 60)

    # Executar fluxo completo
    exemplo_uso_completo()

    # Para baixar datasets específicos, descomente:
    #
    # DATASETS NORMAIS (SEM DOENÇAS) - NOMES CORRETOS:
    print("\n### Para baixar, descomente uma das linhas abaixo: ###\n")
    print("# Soja (nome correto!):")
    print(baixar_dataset('soybean_weed_uav_brazil', './datasets/soja'))
    print("# ")
    print("# Milho/Mudas (nome correto!):")
    print(baixar_dataset('plant_seedlings_aarhus', './datasets/mudas'))
    print("# ")
    print("# Plantas Daninhas:")
    print("# baixar_dataset('crop_weeds', './datasets/plantas_daninhas')")
    print("# ")
    print("# Detecção de Uvas (funciona!):")
    print("# baixar_dataset('grape_detection_californiaday', './datasets/uvas')")
    print("#")
    print("# Ou teste rapidamente um dataset:")
    print("# carregar_e_explorar_dataset('soybean_weed_uav_brazil')")

    print("\n" + "=" * 60)
    print("Script concluído!")
    print("=" * 60)