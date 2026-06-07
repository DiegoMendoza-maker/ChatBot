package com.damo.chatbot

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinishWizard: (String, String, String, String, String, String) -> Unit
) {
    // 1. Estados para guardar toda la información
    var pasoActual by remember { mutableIntStateOf(0) }
    var nombre by remember { mutableStateOf("") }
    var jefeFinal by remember { mutableStateOf("") }
    var reaccion by remember { mutableStateOf("") }
    var escape by remember { mutableStateOf("") }
    var materiaTexto by remember { mutableStateOf("") }
    var metaTexto by remember { mutableStateOf("") }

    // 2. El contenedor mágico que anima las transiciones
    AnimatedContent(
        targetState = pasoActual,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        transitionSpec = {
            // Animación: Entra deslizando desde la derecha, sale hacia la izquierda
            (slideInHorizontally(animationSpec = tween(500)) { width -> width } + fadeIn(tween(500))).togetherWith(
                slideOutHorizontally(animationSpec = tween(500)) { width -> -width } + fadeOut(tween(500))
            )
        },
        label = "OnboardingAnimation"
    ) { paso ->
        // Todo el contenido se centrará en la pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (paso) {
                0 -> {
                    Text("Para empezar, ¿Cómo te gustaria que te llamara?")
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { pasoActual++ },
                        enabled = nombre.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UserBubbleColor)
                    ) { Text("Siguiente") }
                }

                1 -> {
                    // PASO 1: PESAR
                    Text("¿Qué es lo que más te pesa cuando piensas en la escuela?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    val opcionesJefe = listOf(
                        "\uD83D\uDE24 Sentirme atascado sin saber cómo avanzar",
                        "\uD83D\uDE30 El miedo a fallarle a las personas que confían en mí",
                        "\uD83D\uDE1E La sensación de que los demás van más adelantados que yo",
                        "\uD83E\uDEAB El agotamiento que se acumula y no desaparece"
                    )
                    opcionesJefe.forEach { opcion ->
                        OptionButton(text = opcion, isSelected = jefeFinal == opcion) { jefeFinal = opcion }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { pasoActual++ },
                        enabled = jefeFinal.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Siguiente") }
                }

                2 -> {
                    // PASO 2: REACCIÓN
                    Text("Cuando la presión se vuelve demasiada, ¿qué hace tu mente?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    val opcionesReaccion = listOf(
                        "\uD83E\uDDF1 Se apaga — me quedo en blanco y no puedo hacer nada",
                        "\uD83C\uDF00 Se acelera — mil pensamientos negativos al mismo tiempo",
                        "\uD83D\uDD25 Se voltea contra mí — me critico y me exijo más de lo que puedo",
                        "\uD83C\uDFC3 Sale corriendo — busco cualquier distracción para no pensar"
                    )
                    opcionesReaccion.forEach { opcion ->
                        OptionButton(text = opcion, isSelected = reaccion == opcion) { reaccion = opcion }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { pasoActual++ },
                        enabled = reaccion.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UserBubbleColor)
                    ) { Text("Siguiente") }
                }

                3 -> {
                    // PASO 3: ESCAPE
                    Text("Cuando necesitas desconectarte del mundo un momento, ¿a qué recurres?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    val opcionesEscape = listOf(
                        "\uD83C\uDFA7 Música — me pongo audífonos y desaparezco",
                        "\uD83C\uDFAE Videojuegos — entro a otro mundo completamente",
                        "\uD83D\uDCFA Series o anime — me dejo llevar por otra historia",
                        "\uD83C\uDFC3 Moverme — ejercicio, salir, hacer algo físico"
                    )
                    opcionesEscape.forEach { opcion ->
                        OptionButton(text = opcion, isSelected = escape == opcion) { escape = opcion }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { pasoActual++ },
                        enabled = escape.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UserBubbleColor)
                    ) { Text("Siguiente") }
                }

                4 -> {
                    // PASO 4: MATERIA ESPECÍFICA
                    Text("¿Hay alguna materia o proyecto en específico que te quite el sueño en estos momentos?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = materiaTexto,
                        onValueChange = { materiaTexto = it },
                        placeholder = { Text("Ej. Ingeniería Económica, un proyecto de Kotlin...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { pasoActual++ },
                        enabled = materiaTexto.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UserBubbleColor)
                    ) { Text("Siguiente") }
                }

                5 -> {
                    // PASO 5: LA META
                    Text("En tus peores días, ¿qué imagen o persona te hace seguir adelante?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = metaTexto,
                        onValueChange = { metaTexto = it },
                        placeholder = { Text("Ej. Ser independiente, darle el título a mi familia...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        // AQUÍ ENVIAMOS TODOS LOS DATOS JUNTOS
                        onClick = { onFinishWizard(nombre, jefeFinal, reaccion, escape, materiaTexto, metaTexto) },
                        enabled = metaTexto.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UserBubbleColor)
                    ) { Text("¡Conocer a mi asistente!") }
                }
            }
        }
    }
}

// El botón personalizado para las opciones
@Composable
fun OptionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.Transparent,
            contentColor = if (isSelected) Color(0xFF1976D2) else Color.DarkGray
        )
    ) {
        Text(text, textAlign = TextAlign.Center)
    }
}