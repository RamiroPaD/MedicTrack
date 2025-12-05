# MedicTrack

Esta es una aplicacion de recordatorio de toma de medicamentos diriida a quellas personas que padecen alguna enfermedad y mantienen una vida demaciado ocupada o agetriada y suelen olvidar tomar sus medicamentos esta aplicacion ayudara a recordarle la hora de la toma de su medicamento al igual que tiene una funcion de alergias a medicamento que al registrar tu alergia algun medicamento si lo quieres agregar para tomar te mandar una alerta de que eres alergico a este medicamentto

## Caracteristicas
1. Diseño innovador
2. con UX
3. ergonomico para el publico al que esta dirigido
4. Historial
5. Almacenamiento en la nube
6. Alera de alergias
7. Sistema de autenticacion seguro
8. Segiguimiento de aderencia en el medicamento

## Capturas de Pantalla
![Pantallas](docs.pdf)

## Tecnologias Utilizadas
- Kotlin
- Jetpack Compose
- MVVM
- Firebase
- Android Studio

## Instalación
1. Clona este repositorio:
   https://github.com/RamiroPaD/MedicTrack.git
2. Abre el proyecto en Android Studio.
3. Espera a que Gradle termine de compilar.
4. Ejecuta la app en un emulador o dispositivo físico.

## Estructura del Proyecto
 ESTRUCTURA DE CARPETAS:
```plaintext
app/src/main/java/mx/edu/utng/rpd/meditrack/
│
├── models/
│   ├── Usuario.kt
│   ├── Medicamento.kt
│   ├── Recordatorio.kt
│   ├── Historial.kt
│   ├── Alergia.kt
│   └── MedicamentoAPI.kt
│
├── repository/
│   ├── FirebaseRepository.kt
│   └── MedicamentosAPIRepository.kt
│
├── viewmodels/
│   ├── AuthViewModel.kt
│   ├── MedicamentosViewModel.kt
│   └── RecordatoriosViewModel.kt
│
├── services/
│   ├── MediTrackMessagingService.kt
│   └── MedicationAlarmReceiver.kt
│
├── utils/
│   └── NotificationHelper.kt
│
├── MediTrackApplication.kt
└── MainActivity.kt
```
---
 
## 📘 Ejemplos de Código Documentado (KDoc / JSDoc)

| Ejemplo | Archivo |
|--------|----------|
| Ejemplo 1 | [Ejemplo1.kt](Ejemplo1.kt) |
| Ejemplo 2 | [Ejemplo2.kt](Ejemplo2.kt) |

## Links de video y Google Forms
| Nombre| Link |
|--------|----------|
| Encuesta | [[GoogleForms](https://docs.google.com/forms/d/e/1FAIpQLSeUkg87dMbwl0cXoMmAwbXHVL5Z_OjIhCUiU4L5DVxuuwFStQ/viewform?usp=header) |
| Demostracion | [Youtube](https://youtu.be/KyDXJvJALZM) |
## Autores
- Ramiro Padierna Delgado
- Carlos Emanuel Valentino Martinez
