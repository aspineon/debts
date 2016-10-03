Simple process manager used in Shop Item application. Builds its own small read model and based on information from this view throws command towards aggregate root (ShopItem)

Commands are thrown with the help of spring stream and kafka broker