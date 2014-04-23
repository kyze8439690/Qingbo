# Database #

----------

### UserInfo ###
读取到的所有用户信息，包括好友与非好友，读取即保存，以便读取缓存时调用（缓存包含好友与非好友，所以皆保存）  

数据项：

- uid 
- screen_name  
- location  
- statuses_count  
- description  
- followers_count  
- avatar  
- cover  
- friends_count  

----------

### Statuses ###
好友发布的微博

数据项：

- id
- text
- uid
- topics
- time
- comment_count
- repost_count
- pics
- repost_status_id

----------

### Comments ###
微博评论

数据项：

- id
- status_id
- uid
- text
- time

----------

### RepostStatuses ###
转发的微博

数据项：

- id
- text
- uid
- topics
- time
- comment_count
- repost_count
- pics