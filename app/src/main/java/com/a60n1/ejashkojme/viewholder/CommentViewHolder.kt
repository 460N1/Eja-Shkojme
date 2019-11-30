@file:Suppress("JoinDeclarationAndAssignment")

package com.a60n1.ejashkojme.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.R
import de.hdodenhof.circleimageview.CircleImageView

class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var authorAvatar: CircleImageView
    var authorView: TextView
    var bodyView: TextView

    init {
        authorAvatar = itemView.findViewById(R.id.comment_photo)
        authorView = itemView.findViewById(R.id.comment_author)
        bodyView = itemView.findViewById(R.id.comment_body)
    }
}