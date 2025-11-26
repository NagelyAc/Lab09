package com.example.lab09

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// --- 1. PANTALLA DE LISTADO: ScreenPosts ---
@Composable
fun ScreenPosts(navController: NavHostController, servicio: PostApiService) {
    var listaPosts = remember { mutableStateListOf<PostModel>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // CORREGIDO: Se obtiene el objeto contenedor y se extrae la lista 'posts'
            val response = servicio.getUserPosts()
            listaPosts.addAll(response.posts)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error al cargar posts: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (listaPosts.isEmpty()) {
            Text("No se encontraron posts.", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(contentPadding = PaddingValues(8.dp)) {
                items(listaPosts) { item ->
                    PostCard(item, onClick = {
                        navController.navigate("postsVer/${item.id}")
                    })
                }
            }
        }
    }
}

// --- 2. COMPONENTE LLAMATIVO: PostCard (Tarjeta de la Lista) ---
@Composable
fun PostCard(post: PostModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Metadata y Usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Usuario ID: ${post.userId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "#${post.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            // Título
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))

            // Cuerpo
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            // Reacciones (CORREGIDO y con Íconos)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Likes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Likes",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${post.reactions.likes}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Dislikes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "Dislikes",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${post.reactions.dislikes}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Etiquetas (resaltada)
                Text(
                    text = post.tags.firstOrNull() ?: "Sin Etiqueta",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// --- 3. PANTALLA DE DETALLE: ScreenPost (Mejorada) ---
@Composable
fun ScreenPost(navController: NavHostController, servicio: PostApiService, id: Int) {
    var post by remember { mutableStateOf<PostModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(id) {
        try {
            post = servicio.getUserPostById(id)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Post ID $id no encontrado: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (post != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Título Principal
            Text(
                text = post!!.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Metadata: ID y Usuario
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Post ID: #${post!!.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Usuario: ${post!!.userId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            // Tarjeta para el Cuerpo del Post
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cuerpo del Post:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = post!!.body,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tarjeta para Reacciones y Etiquetas
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Datos Adicionales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Divider(Modifier.padding(vertical = 8.dp))

                    // Reacciones
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        DetailItemWithIcon(
                            icon = Icons.Default.ThumbUp,
                            label = "Likes",
                            value = post!!.reactions.likes.toString(),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        DetailItemWithIcon(
                            icon = Icons.Default.ThumbDown,
                            label = "Dislikes",
                            value = post!!.reactions.dislikes.toString(),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Divider(Modifier.padding(vertical = 8.dp))

                    // Etiquetas
                    DetailItem(label = "Etiquetas", value = post!!.tags.joinToString())
                }
            }
        }
    } else {
        Text("Post no encontrado.", modifier = Modifier.padding(16.dp))
    }
}

// --- 4. COMPONENTE AUXILIAR: Ítem de Detalle (con Icono) ---
@Composable
fun DetailItemWithIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// --- 5. COMPONENTE AUXILIAR: Ítem de Detalle (Básico) ---
@Composable
fun DetailItem(label: String, value: String, singleLine: Boolean = true) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE
        )
    }
}