package mx.edu.utng.rpd.meditrack.repository

import mx.edu.utng.rpd.meditrack.models.MedicamentoAPI

// ============================================================================
// repository/MedicamentosAPIRepository.kt - NUEVO ARCHIVO
// ============================================================================

object MedicamentosAPIRepository {

    private val medicamentosDB = listOf(
        MedicamentoAPI(
            nombre = "Paracetamol",
            concentraciones = listOf("500 mg", "650 mg", "1 g"),
            presentaciones = listOf("Tabletas", "Cápsulas", "Suspensión", "Gotas"),
            usos = "Analgésico y antipirético"
        ),
        MedicamentoAPI(
            nombre = "Ibuprofeno",
            concentraciones = listOf("200 mg", "400 mg", "600 mg", "800 mg"),
            presentaciones = listOf("Tabletas", "Cápsulas", "Suspensión", "Gel"),
            usos = "Antiinflamatorio y analgésico"
        ),
        MedicamentoAPI(
            nombre = "Amoxicilina",
            concentraciones = listOf("250 mg", "500 mg", "875 mg", "1 g"),
            presentaciones = listOf("Cápsulas", "Tabletas", "Suspensión"),
            usos = "Antibiótico"
        ),
        MedicamentoAPI(
            nombre = "Omeprazol",
            concentraciones = listOf("10 mg", "20 mg", "40 mg"),
            presentaciones = listOf("Cápsulas", "Tabletas"),
            usos = "Inhibidor de la bomba de protones"
        ),
        MedicamentoAPI(
            nombre = "Losartán",
            concentraciones = listOf("25 mg", "50 mg", "100 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antihipertensivo"
        ),
        MedicamentoAPI(
            nombre = "Metformina",
            concentraciones = listOf("500 mg", "850 mg", "1000 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antidiabético"
        ),
        MedicamentoAPI(
            nombre = "Atorvastatina",
            concentraciones = listOf("10 mg", "20 mg", "40 mg", "80 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Hipolipemiante"
        ),
        MedicamentoAPI(
            nombre = "Clonazepam",
            concentraciones = listOf("0.5 mg", "1 mg", "2 mg"),
            presentaciones = listOf("Tabletas", "Gotas"),
            usos = "Ansiolítico"
        ),
        MedicamentoAPI(
            nombre = "Loratadina",
            concentraciones = listOf("10 mg"),
            presentaciones = listOf("Tabletas", "Jarabe"),
            usos = "Antihistamínico"
        ),
        MedicamentoAPI(
            nombre = "Ranitidina",
            concentraciones = listOf("150 mg", "300 mg"),
            presentaciones = listOf("Tabletas", "Jarabe"),
            usos = "Antiácido"
        ),
        MedicamentoAPI(
            nombre = "Captopril",
            concentraciones = listOf("25 mg", "50 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antihipertensivo"
        ),
        MedicamentoAPI(
            nombre = "Salbutamol",
            concentraciones = listOf("2 mg", "4 mg", "100 mcg"),
            presentaciones = listOf("Tabletas", "Jarabe", "Inhalador"),
            usos = "Broncodilatador"
        ),
        MedicamentoAPI(
            nombre = "Diclofenaco",
            concentraciones = listOf("50 mg", "75 mg", "100 mg"),
            presentaciones = listOf("Tabletas", "Gel", "Inyección"),
            usos = "Antiinflamatorio"
        ),
        MedicamentoAPI(
            nombre = "Tramadol",
            concentraciones = listOf("50 mg", "100 mg"),
            presentaciones = listOf("Cápsulas", "Tabletas", "Gotas"),
            usos = "Analgésico opioide"
        ),
        MedicamentoAPI(
            nombre = "Azitromicina",
            concentraciones = listOf("250 mg", "500 mg"),
            presentaciones = listOf("Tabletas", "Suspensión"),
            usos = "Antibiótico"
        ),
        MedicamentoAPI(
            nombre = "Cetirizina",
            concentraciones = listOf("5 mg", "10 mg"),
            presentaciones = listOf("Tabletas", "Jarabe", "Gotas"),
            usos = "Antihistamínico"
        ),
        MedicamentoAPI(
            nombre = "Naproxeno",
            concentraciones = listOf("250 mg", "500 mg", "550 mg"),
            presentaciones = listOf("Tabletas", "Suspensión"),
            usos = "Antiinflamatorio"
        ),
        MedicamentoAPI(
            nombre = "Prednisona",
            concentraciones = listOf("5 mg", "10 mg", "20 mg", "50 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Corticoide"
        ),
        MedicamentoAPI(
            nombre = "Enalapril",
            concentraciones = listOf("5 mg", "10 mg", "20 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antihipertensivo"
        ),
        MedicamentoAPI(
            nombre = "Glibenclamida",
            concentraciones = listOf("5 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antidiabético"
        ),
        MedicamentoAPI(
            nombre = "Aspirina",
            concentraciones = listOf("100 mg", "500 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antiagregante plaquetario"
        ),
        MedicamentoAPI(
            nombre = "Ciprofloxacino",
            concentraciones = listOf("250 mg", "500 mg", "750 mg"),
            presentaciones = listOf("Tabletas", "Suspensión"),
            usos = "Antibiótico"
        ),
        MedicamentoAPI(
            nombre = "Dexametasona",
            concentraciones = listOf("0.5 mg", "0.75 mg", "4 mg"),
            presentaciones = listOf("Tabletas", "Inyección"),
            usos = "Corticoide potente"
        ),
        MedicamentoAPI(
            nombre = "Furosemida",
            concentraciones = listOf("20 mg", "40 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Diurético"
        ),
        MedicamentoAPI(
            nombre = "Gabapentina",
            concentraciones = listOf("300 mg", "400 mg", "600 mg"),
            presentaciones = listOf("Cápsulas", "Tabletas"),
            usos = "Anticonvulsivante"
        ),
        MedicamentoAPI(
            nombre = "Hidroclorotiazida",
            concentraciones = listOf("25 mg", "50 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Diurético"
        ),
        MedicamentoAPI(
            nombre = "Ketorolaco",
            concentraciones = listOf("10 mg", "30 mg"),
            presentaciones = listOf("Tabletas", "Inyección"),
            usos = "Analgésico potente"
        ),
        MedicamentoAPI(
            nombre = "Levotiroxina",
            concentraciones = listOf("25 mcg", "50 mcg", "75 mcg", "100 mcg"),
            presentaciones = listOf("Tabletas"),
            usos = "Hormona tiroidea"
        ),
        MedicamentoAPI(
            nombre = "Meloxicam",
            concentraciones = listOf("7.5 mg", "15 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antiinflamatorio"
        ),
        MedicamentoAPI(
            nombre = "Pantoprazol",
            concentraciones = listOf("20 mg", "40 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Inhibidor de bomba de protones"
        ),
        MedicamentoAPI(
            nombre = "Ranitidina",
            concentraciones = listOf("150 mg", "300 mg"),
            presentaciones = listOf("Tabletas", "Jarabe"),
            usos = "Antiácido"
        ),
        MedicamentoAPI(
            nombre = "Sertralina",
            concentraciones = listOf("50 mg", "100 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antidepresivo"
        ),
        MedicamentoAPI(
            nombre = "Simvastatina",
            concentraciones = listOf("10 mg", "20 mg", "40 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Hipolipemiante"
        ),
        MedicamentoAPI(
            nombre = "Telmisartán",
            concentraciones = listOf("40 mg", "80 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Antihipertensivo"
        ),
        MedicamentoAPI(
            nombre = "Warfarina",
            concentraciones = listOf("1 mg", "2 mg", "5 mg"),
            presentaciones = listOf("Tabletas"),
            usos = "Anticoagulante"
        )
    )

    fun buscarMedicamentos(query: String): List<MedicamentoAPI> {
        if (query.isBlank()) return emptyList()
        return medicamentosDB.filter {
            it.nombre.contains(query, ignoreCase = true)
        }.take(5)
    }

    fun obtenerMedicamento(nombre: String): MedicamentoAPI? {
        return medicamentosDB.find { it.nombre.equals(nombre, ignoreCase = true) }
    }
}