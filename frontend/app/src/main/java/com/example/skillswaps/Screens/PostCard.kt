package com.example.skillswaps.Screens


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.skillswaps.dataclasses.CommunityPost
import com.example.skillswaps.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PostCard(post: CommunityPost, onClick: () -> Unit) {
    val font1 = FontFamily(Font(R.font.font1, FontWeight.SemiBold))
    val font2 = FontFamily(Font(R.font.font2, FontWeight.Medium))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            Color(0xFFFFD95A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title centered at top
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                fontFamily = font1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
                , fontWeight = FontWeight.Bold, letterSpacing = 0.0.sp
            )
            Spacer(modifier=Modifier.height(10.dp))

            // Offer & Want on left, Post Image on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Left column: Offer and Want (or content)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (post.type.lowercase(Locale.getDefault()) == "exchange") {
                        post.offer?.takeIf { it.isNotBlank() }?.let { offerText ->
                            Row {
                                Text("Offer:  ", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text(
                                    text = offerText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color=Color.DarkGray, lineHeight = 19.sp,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis, fontFamily = font2

                                )
                            }
                        }
                        post.want?.takeIf { it.isNotBlank() }?.let { wantText ->
                            Row {
                                Text("Want:   ", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text(
                                    text = wantText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color=Color.DarkGray, lineHeight = 19.sp,
                                    maxLines = 5
                                    , overflow = TextOverflow.Ellipsis, fontFamily = font2
                                    )
                            }
                        }
                    } else {
                        // For other types, show content or a snippet
                        if (post.content.isNotBlank()) {
                            Text(
                                text = post.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                    }
                }

                // Right: Post image
                if (post.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    // If no image URL, you could show a placeholder box or nothing
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Image",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Tags row (non-clickable chips)
            if (post.tags.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // spacing between rows
                ) {
                    Spacer(modifier=Modifier.height(8.dp))
                    // Break tags into sublists of size 3
                    post.tags.chunked(3).forEach { tagRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // spacing between items in a row
                        ) {
                            tagRow.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            color = Color.Gray,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .background(
                                            Color.White,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.bodySmall,
                                        color =Color.Black
                                    )
                                }
                            }
                            // Optionally, if you want to fill remaining space so that tags are evenly spaced,
                            // you could add empty Spacer(Modifier.weight(1f)) here when tagRow.size < 3.
                            // But usually wrap-content is fine.
                        }
                    }
                }
            }


            // Spacer before bottom row
            Spacer(modifier = Modifier.height(14.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Category on bottom-left
                    if (post.category.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color =Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                ).background(shape =  RoundedCornerShape(12.dp), color = Color(76,61,61,255))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = post.category,

                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,


                            )
                        }
                    }



                    // Location with pin icon (if present)
                    if (post.location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(painter =painterResource(R.drawable.baseline_location_pin_24)
                                , contentDescription = null,
                                modifier=Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Timestamp (if available)
                    post.timestamp?.toFormattedString()?.takeIf { it.isNotBlank() }?.let { timeStr ->
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp), fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Author name on bottom-right

                }
                Spacer(modifier=Modifier.height(8.dp))
                Row(modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End){
                    if (post.authorName.isNotEmpty()) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp), fontWeight = FontWeight.SemiBold
                        )
                    }
                }

            }// Bottom row: category (left), location & timestamp center (optional), author name & time right

        }
    }
}
fun Timestamp?.toFormattedString(): String {
    return this?.let {
        val date = it.toDate()
        // Example format: "dd MMM yyyy, HH:mm"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(date)
    } ?: ""
}
