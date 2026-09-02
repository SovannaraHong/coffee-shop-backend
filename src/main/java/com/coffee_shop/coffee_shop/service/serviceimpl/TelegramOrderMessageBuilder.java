package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.entity.Order;
import com.coffee_shop.coffee_shop.entity.OrderDetail;
import com.coffee_shop.coffee_shop.entity.OrderDetailAddon;
import org.springframework.stereotype.Component;

@Component
public class TelegramOrderMessageBuilder {

    /**
     * Sent once the payment webhook/poll confirms status == PAID.
     * This is the ONLY Telegram message in the flow — unpaid/abandoned orders never notify,
     * to avoid spamming the channel with orders that never convert.
     * This carries the full order breakdown, same as the old "new order" message.
     */
    public String buildOrderPaidMessage(Order order, Customer customer) {
        StringBuilder sb = new StringBuilder();

        sb.append("✅ *Payment Received — Order Confirmed!*\n");
        sb.append("━━━━━━━━━━━━━━━━\n");
        sb.append("👤 *Customer Info*\n");
        sb.append("🗿 Name: ").append(esc(customer.getFirstName())).append(" ").append(esc(customer.getLastName())).append("\n");
        if (customer.getEmail() != null) {
            sb.append("📧 Email: `").append(esc(customer.getEmail())).append("`\n");
        }
        if (customer.getPhone() != null) {
            sb.append("📲 Phone: `").append(esc(customer.getPhone())).append("`\n");
        }
        sb.append("━━━━━━━━━━━━━━━━\n\n");

        sb.append("🛒 *ORDER ITEMS*\n");
        sb.append("┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄\n");

        for (OrderDetail detail : order.getOrderDetails()) {
            sb.append("🏷️ ").append(esc(detail.getProductVariant().getProduct().getName()))
                    .append(" (").append(esc(detail.getProductVariant().getName())).append(")\n");
            sb.append("💰 Price: `$").append(detail.getUnitPrice()).append("`\n");
            sb.append("🔢 Qty: `").append(detail.getQuantity()).append("`\n");
            sb.append("🧾 Subtotal: `$").append(detail.getSubtotal()).append("`\n");

            if (!detail.getOrderDetailAddons().isEmpty()) {
                sb.append("   ➕ Add-ons:\n");
                for (OrderDetailAddon addon : detail.getOrderDetailAddons()) {
                    sb.append("      • ").append(esc(addon.getAddon().getName()))
                            .append(" x").append(addon.getQuantity())
                            .append(" — $").append(addon.getSubtotal()).append("\n");
                }
            }
            sb.append("┄┄┄┄┄┄┄┄┄┄┄┄\n");
        }

        sb.append("\n💵 *Total:* `$").append(order.getFinalAmount()).append("`\n");
        if (order.getNote() != null && !order.getNote().isBlank()) {
            sb.append("📝 Note: ").append(esc(order.getNote())).append("\n");
        }
        sb.append("🧾 Order #: `").append(esc(order.getOrderNumber())).append("`\n");
        sb.append("🕐 Payment confirmed just now");

        return sb.toString();
    }

    /**
     * Escapes characters that break Telegram's legacy "Markdown" parse_mode
     * when they appear in user-supplied text (names, notes, product names, etc.).
     * Reserved chars for legacy Markdown: _ * ` [
     */
    private String esc(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("[", "\\[");
    }
}