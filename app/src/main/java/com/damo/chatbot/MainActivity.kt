package com.damo.chatbot

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damo.chatbot.ui.theme.ChatBotTheme
import com.damo.chatbot.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
//Para darle personalidad
import com.google.ai.client.generativeai.type.content
//Para guardar el nombre
import android.content.Context
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.TextButton
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.ui.text.style.TextAlign
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
//para gifs
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.decode.GifDecoder


// Definimos los estados posibles de nuestra app
enum class AppState { Welcome, Chat }

// Definimos colores personalizados basados en el diseño de la imagen
val BotBubbleColor = Color(0xFFF1F1F1) // Gris claro
val UserBubbleColor = Color(0xFF2196F3) // Azul
val BotTextColor = Color.Black
val UserTextColor = Color.White
val TimeTextColor = Color.Gray

// Instancia global que ya tenías (mantenerla aquí está bien para este ejemplo)
val generativeModel = GenerativeModel(
    modelName = "gemini-3.5-flash",
    apiKey = BuildConfig.GEMINI_API_KEY,
    systemInstruction = content {
        text("Eres un amigo virtual súper empático, cálido y muy positivo.\n" +
                "Tu único objetivo es dar apoyo emocional, escuchar activamente y levantarle el ánimo al usuario cuando se sienta triste, estresado o abrumado.\n" +
                "Debes permitir al usuario expresar sus miedos e inquietudes en un entorno estudiantil y debes darle una retroalimentación emocional.\n" +
                "¡Unicamente debes dar retoalimentación emocional!\n" +
                "Comunícate de forma muy casual, cercana y relajada, usando jerga mexicana amigable si es natural. \n" +
                "Nunca suenes como un robot formal o un doctor.\n" +
                "Usa emojis de vez en cuando para transmitir cariño y confianza.\n" +
                "Queda prohibido dar recomendaciones como matar a al profesor, matar a los compañeros, suicidarse o unirse a un cartel de delincuentes.\n" +
                "No respondas en mas de 3 lineas de dialogo." +
                "Quiero que cada nueva interacción te vayas adaptando a la forma de hablar de la persona, peor sin llegar a las grocerias"
        )
    }
)

class MainActivity : ComponentActivity() {

    // Inicializamos el ViewModel usando la delegación 'viewModels()'
    private val chatViewModel: ChatViewModel by viewModels()

    // Declaramos el motor de voz
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializamos el motor con el acento local (ej. Español de México)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "MX") // Usa el acento mexicano
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita diseño transparente de barras de estado

        // Accedemos a las preferencias guardadas del teléfono
        val sharedPreferences = getSharedPreferences("ChatBotPrefs", Context.MODE_PRIVATE)
        // Buscamos el nombre. Si no existe, devuelve un texto vacío ("")
        val nombreGuardado = sharedPreferences.getString("NOMBRE_USUARIO", "") ?: ""

        setContent {
            ChatBotTheme {
                // Variables de estado para controlar la pantalla actual y guardar el nombre
                // Decidimos la pantalla inicial basados en si hay un nombre guardado
                var currentState by remember {
                    mutableStateOf(if (nombreGuardado.isEmpty()) AppState.Welcome else AppState.Chat)
                }
                var userName by remember { mutableStateOf(nombreGuardado) }

                // Efecto automático: Si ya había nombre y entramos directo al chat, que salude
                LaunchedEffect(Unit) {
                    if (currentState == AppState.Chat && nombreGuardado.isNotEmpty()) {
                        chatViewModel.iniciarChatConNombre(nombreGuardado)
                    }
                }

                // El interruptor principal
                when (currentState) {
                    AppState.Welcome -> {
                        WelcomeScreen(
                            nombreValue = userName,
                            onNombreChange = { userName = it },
                            onEntrarClick = {
                                val nombreFinal = userName.trim()
                                // ¡AQUÍ GUARDAMOS EL NOMBRE PARA SIEMPRE EN EL TELÉFONO!
                                sharedPreferences.edit().putString("NOMBRE_USUARIO", nombreFinal).apply()
                                // Al darle clic al botón, le pasamos el nombre al bot
                                chatViewModel.iniciarChatConNombre(userName.trim())
                                // Y cambiamos la pantalla al chat
                                currentState = AppState.Chat
                            }
                        )
                    }
                    AppState.Chat -> {
                        // Variable que controla el interruptor (true = empieza con el muñequito)
                        var isAvatarMode by remember { mutableStateOf(true) }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                // Tu cabecera original intacta (sin los 3 puntos)
                                ChatHeader()
                            },
                            bottomBar = {
                                // Agrupamos el botón nuevo y la barra de texto en la parte inferior
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // AQUÍ ESTÁ EL NUEVO BOTÓN INTERRUPTOR
                                    TextButton(
                                        onClick = { isAvatarMode = !isAvatarMode },
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isAvatarMode) Icons.Default.List else Icons.Default.Face,
                                            contentDescription = "Cambiar vista",
                                            modifier = Modifier.size(18.dp),
                                            tint = UserBubbleColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isAvatarMode) "Ver historial de chat" else "Ver muñequito animado",
                                            color = UserBubbleColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Tu barra de texto con el micrófono (se queda igual)
                                    UserInputBar(
                                        textValue = chatViewModel.userInputText,
                                        onTextChange = { chatViewModel.onUserInputChanged(it) },
                                        onSendClick = { chatViewModel.sendMessage() },
                                        isLoading = chatViewModel.isLoading
                                    )
                                }
                            }
                        ) { innerPadding ->
                            // Este es el "Switch" mágico que cambia la vista central
                            if (isAvatarMode) {
                                AvatarScreen(
                                    messages = chatViewModel.messages,
                                    isLoading = chatViewModel.isLoading,
                                    // Le mandamos la función para hablar
                                    speakText = { textoCompleto ->
                                        // 1. Limpiamos el texto de emojis primero
                                        val textoLimpio = limpiarTextoParaVoz(textoCompleto)

                                        // 2. Si quedó texto (por si el mensaje era SOLO emojis), lo hablamos
                                        if (textoLimpio.isNotBlank()) {
                                            tts?.speak(textoLimpio, TextToSpeech.QUEUE_FLUSH, null, "")
                                        }
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            } else {
                                ChatMessagesList(
                                    messages = chatViewModel.messages,
                                    isLoading = chatViewModel.isLoading,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        // Apagamos la voz al salir de la app
        tts?.shutdown()
        super.onDestroy()
    }
}

// --- COMPONENTES UI COMPOSE ---

/**
 * Cabecera del Chat (replicando image_0.png)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Usa Image para fotos o avatares a color, caso contrario, usar Icon
                Image(
                    painter = painterResource(id = R.drawable.perfil), // Aquí está el truco
                    contentDescription = "Mi foto de perfil",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Circuit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Online",
                        color = Color(0xFF4CAF50), // Verde online
                        fontSize = 12.sp
                    )
                }
            }
        },
        //actions = {
        //    IconButton(onClick = { /* Acción menú */ }) {
        //        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menú")
        //    }
        //},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Lista scrolleable de mensajes
 */
@Composable
fun ChatMessagesList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Efecto lanzado: Scrollear automáticamente al final cuando llega un nuevo mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9)) // Fondo muy claro para el chat
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubbleItem(message = message)
        }

        // Mostrar indicador de carga si el bot está pensando
        if (isLoading) {
            item {
                BotTypingIndicator()
            }
        }
    }
}

/**
 * Representa una burbuja de mensaje individual (Bot o Usuario)
 */
@Composable
fun MessageBubbleItem(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            // Mostrar avatar solo para el Bot
            if (!message.isUser) {
                Image(
                    painter = painterResource(id = R.drawable.perfil), // Aquí está el truco
                    contentDescription = "Mi foto de perfil",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }

            // La burbuja de texto
            Surface(
                color = if (message.isUser) UserBubbleColor else BotBubbleColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 0.dp, // Esquina cortada según remitente
                    bottomEnd = if (message.isUser) 0.dp else 16.dp
                ),
                tonalElevation = 1.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (message.isUser) UserTextColor else BotTextColor,
                    fontSize = 15.sp
                )
            }
        }

        // Hora del mensaje
        Text(
            text = message.time,
            color = TimeTextColor,
            fontSize = 11.sp,
            modifier = Modifier.padding(
                start = if (message.isUser) 0.dp else 40.dp, // Alinear bajo la burbuja, saltando avatar
                end = if (message.isUser) 8.dp else 0.dp,
                top = 2.dp
            )
        )
    }
}

/**
 * Indicador visual simple de que el bot está escribiendo
 */
@Composable
fun BotTypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.perfil), // Aquí está el truco
            contentDescription = "Mi foto de perfil",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Text(text = "Circuit pensando...", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(8.dp))
        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.Gray)
    }
}

/**
 * Barra inferior de entrada de texto (replicando image_0.png)
 */

@Composable
fun UserInputBar(
    textValue: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    // 1. Preparamos el "lanzador" que recibirá el texto de tu voz
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val textoHablado = matches[0]
                // Si ya habías escrito algo, le suma un espacio y tu voz. Si estaba vacío, pone solo tu voz.
                val nuevoTexto = if (textValue.isEmpty()) textoHablado else "$textValue $textoHablado"
                onTextChange(nuevoTexto)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().imePadding(),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Icono de + :
            //IconButton(onClick = { /* Adjuntar */ }) {
            //    Icon(painter = painterResource(id = android.R.drawable.ic_input_add), contentDescription = "Adjuntar", tint = Color.Gray)
            //}

            // 2. El botón del Micrófono ahora ejecuta el Intent de voz nativo de Android
            IconButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    // Usará el idioma en el que esté configurado el teléfono
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Te escucho...")
                }
                try {
                    speechLauncher.launch(intent)
                } catch (e: Exception) {
                    // Por si lo pruebas en un emulador que no tiene app de Google instalada
                    println("El dispositivo no soporta entrada de voz nativa.")
                }
            }) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Voz", tint = Color.Gray)
            }

            // Campo de texto principal
            TextField(
                value = textValue,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = { Text("Escribe un mensaje...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                maxLines = 4,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de enviar
            FilledIconButton(
                onClick = onSendClick,
                enabled = textValue.isNotBlank() && !isLoading,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = UserBubbleColor,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
fun AvatarScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    speakText: (String) -> Unit, // Recibe la función de voz
    modifier: Modifier = Modifier
) {
    // Buscamos el último mensaje del bot para ponerlo debajo del muñequito
    val lastBotMessage = messages.lastOrNull { !it.isUser }?.text ?: "¡Hola! Estoy listo."
    val context = LocalContext.current

    // Cada vez que el mensaje cambie y deje de cargar, lo hablará
    LaunchedEffect(lastBotMessage, isLoading) {
        if (!isLoading) {
            speakText(lastBotMessage)
        }
    }

    // 1. CONFIGURAR EL MOTOR DE COIL PARA REPRODUCIR GIFS
    // Detecta la versión de Android para usar el mejor reproductor disponible
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Cargar gif
        val gifOrigen = if (isLoading) {
            R.drawable.bot_to_think
        } else {
            R.drawable.bot_neutral
        }

        // DIBUJAR EL GIF UTILIZANDO COIL
        Image(
            painter = rememberAsyncImagePainter(model = gifOrigen, imageLoader = imageLoader),
            contentDescription = "Avatar GIF Animado",
            modifier = Modifier.size(300.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Solo mostramos texto cuando está pensando
        if (isLoading) {
            Text(
                text = "Pensando...",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }

        /* Burbuja de texto con lo que dice el bot
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(
                text = if (isLoading) "Pensando..." else lastBotMessage,
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = BotTextColor
            )
        }*/
    }
}

// Función utility para limpiar emojis antes de mandar al motor de voz
fun limpiarTextoParaVoz(texto: String): String {
    // Patrón Regex para encontrar prácticamente todos los emojis Unicode
    // Incluye símbolos, pictografías adicionales, variaciones de color de piel, etc.
    val regexEmojis = Regex(
        "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+" +  // Símbolos y Pictografías Suplementarios
                "|[\\u2600-\\u26FF\\u2700-\\u27BF]+"   // Símbolos Misceláneos y Dingbats
    )

    // Reemplaza todos los emojis encontrados por un espacio vacío y quita espacios extra
    return texto.replace(regexEmojis, "").trim()
}