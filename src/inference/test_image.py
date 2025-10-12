from test_model import PlantDiseasePredictor
predictor = PlantDiseasePredictor('../../checkpoint.pth')
result = predictor.predict_single_image('../../data/val/Saudavel/folha_soja.jpg')
print('Resultado:', result)
