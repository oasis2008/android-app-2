package one.mixin.android.ui.conversation.holder

import android.view.View
import kotlinx.android.synthetic.main.item_chat_card.view.*
import one.mixin.android.vo.MessageItem

class CardHolder constructor(containerView: View) : BaseViewHolder(containerView) {
    override fun chatLayout(isMe: Boolean, isLast: Boolean) {
    }

    fun bind(messageItem: MessageItem) {
        listen(messageItem.messageId)
        itemView.name_tv.text = messageItem.content
    }
}