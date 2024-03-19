По интерфейсу:
из кнопок на фасаде оставить "Заказ", которая вызовет меню выбора по типу заказа,
"Мои заказы", которая вызовет меню работы с заказами, бронированиями и поездками. 
Остальное убрать в главное меню

вместе с предложением бронирования
DEBUG MatchingHandler:97 - method: sendListOfSuitableRideRequestMessage
DEBUG MatchingHandler:124 - method: createListOfSuitableRequestsMessage

водителю приходит
DEBUG MatchingHandler:157 - method sendNoticeAboutSendingBookingMessage
Проверить почему


много времени занимает обработка и сохранение дня поездки в поиске пассажиров

при бронипрвании со стороны пассажира вызывает 
java.lang.NullPointerException: Cannot invoke "by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis.getChatId()" because the return value of "by.ivam.fellowtravelerbot.redis.model.Booking.getFindPassRequestRedis()" is null
