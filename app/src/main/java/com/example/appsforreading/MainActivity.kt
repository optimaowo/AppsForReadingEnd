package com.example.appsforreading

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appsforreading.SupabaseAuthViewModel
import com.example.appsforreading.data.model.UserState
import com.example.appsforreading.ui.theme.AppsForReadingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppsForReadingTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") { AuthScreen(navController) }
                    composable("bookList") { BookListScreen(navController) }
                    composable("bookDetail/{bookTitle}") { backStackEntry ->
                        val bookTitle = backStackEntry.arguments?.getString("bookTitle")
                        BookDetailScreen(bookTitle ?: "")
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(navController: NavHostController) {
    val viewModel: SupabaseAuthViewModel = viewModel()
    val context = LocalContext.current
    val userState by viewModel.userState

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var currentUserState by remember { mutableStateOf("") }

    LaunchedEffect(userState) {
        when (userState) {
            is UserState.Success -> {
                navController.navigate("bookList") {
                    popUpTo("auth") { inclusive = true }
                }
            }
            is UserState.Error -> {
                currentUserState = (userState as UserState.Error).message
            }
            UserState.Idle -> {}
            is UserState.Loading -> {
                currentUserState = "Загрузка..."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = userEmail,
            placeholder = { Text(text = "Enter email") },
            onValueChange = { userEmail = it }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(
            value = userPassword,
            placeholder = { Text(text = "Enter password") },
            onValueChange = { userPassword = it }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = { viewModel.signUp(context, userEmail, userPassword) }) {
            Text(text = "Sign Up")
        }
        Button(onClick = { viewModel.login(context, userEmail, userPassword) }) {
            Text(text = "Login")
        }
        if (currentUserState.isNotEmpty()) {
            Text(text = currentUserState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(navController: NavHostController) {
    val books = remember { mutableStateListOf(
        "Hilarious book-titles & authors",
        "A gossip on book-titles",
        "Pali book-titles and their brief designations",
        "Преступление и наказание",
        "Война и мир",
        "Анна Каренина",
        "Мастер и Маргарита",
        "Тихий Дон",
        "Доктор Живаго",
        "Братья Карамазовы",
        "Собачье сердце",
        "Невский проспект",
        "Идиот",
        "Старик и море",
        "451 градус по Фаренгейту",
        "1984",
        "Убить пересмешника",
        "Гарри Поттер и философский камень",
        "Дон Кихот",
        "Собрание сочинений А.С. Пушкина",
        "Тарас Бульба",
        "Мертвые души",
        "Герой нашего времени",
        "Капитанская дочка"
    ) }

    var searchQuery by remember { mutableStateOf("") }
    val filteredBooks = books.filter { it.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Доступные книги") },
                actions = {
                    IconButton(onClick = { navController.navigate("auth") }) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Logic to add a new book
                books.add("Новая книга") // Here you can add a dialog for book title input
            }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск книг") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(filteredBooks) { book ->
                    BookItem(book) {
                        navController.navigate("bookDetail/$book")
                    }
                }
            }
        }
    }
}

@Composable
fun BookDetailScreen(bookTitle: String) {
    var bookInfo by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(bookTitle) {
        bookInfo = fetchBookInfo(bookTitle)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = bookTitle, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = bookInfo)
    }
}

@Composable
fun BookItem(book: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = book,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

suspend fun fetchBookInfo(bookTitle: String): String {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://openlibrary.org/search.json?title=$bookTitle")
        .build()

    return withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response: Response ->
            if (response.isSuccessful) {
                val jsonObject = JSONObject(response.body?.string() ?: "")
                val docs = jsonObject.getJSONArray("docs")
                if (docs.length() > 0) {
                    val book = docs.getJSONObject(0)
                    return@withContext "Author: ${book.getJSONArray("author_name").join(", ")}\n" +
                            "Published: ${book.getString("first_publish_year")}\n" +
                            "Key: ${book.getString("key")}"
                }
            }
            return@withContext "Информация о книге не найдена."
        }
    }
}