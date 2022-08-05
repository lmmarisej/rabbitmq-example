 //设置队列TTL
 Map<String, Object> args = new HashMap<String, Object>();
 args.put("x-message-ttl", 10000);

//设置单条消息TTL
AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().expiration("100000").build();
 