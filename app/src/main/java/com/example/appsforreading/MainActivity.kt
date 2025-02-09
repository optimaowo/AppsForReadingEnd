package com.example.appsforreading

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookLoverApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookLoverApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "bookList") {
        composable("bookList") { BookListScreen(navController) }
        composable("bookDetail/{bookTitle}") { backStackEntry ->
            val bookTitle = backStackEntry.arguments?.getString("bookTitle")
            BookDetailScreen(bookTitle ?: "")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(navController: NavController) {
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

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Логика добавления новой книги
                books.add("Новая книга") // Здесь можно добавить диалог для ввода названия книги
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