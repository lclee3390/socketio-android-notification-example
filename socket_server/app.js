var app = require('express')()
var server = require('http').createServer(app)
var io = require('socket.io')(server)
const port = 3000;



var users = [];
var data = [];

var id = 0;

io.on('connection', (socket) => { 
    console.log('a user connected.')

    // socket.emit("notification","sdf")

    // console.log(socket)

    // socket.on('chat',(d)=>{
    //     io.emit('chat',d)
    // })

    // socket.on('notification',(d)=>{
    //     console.log('noti!!')
    // })


    socket.on('login',(d)=>{
        console.log('login',d)
        let user = {
            username:d.username,
            socket:socket
        }
        users.push(user)
    })

    socket.on('ok',(d)=>{
        console.log('ok',d)
    })

    // socket.broadcast.emit('')
});


addData = ()=>{
    data.push("hello world")
}

deleteData = ()=>{
    data.pop()
}

findUser = (username)=>{
    const r = users.filter( u=>{
        return u.username === username
    })
    return r;
}

pushData = (username, title, text, notificationId)=>{
    var userList = findUser(username)

    let obj = {
        title:title,
        text:text,
        notificationId:notificationId
    }

    if(userList){
        userList.forEach(element => {
            element.socket.emit("notification", obj)  
        });
    }
}



app.get('/add',function(req,res){
    addData();
    res.json(data)
})

app.get('/delete',function(req,res){
    deleteData();
    res.json(data)
})


app.get('/users',function(req,res){
    let usernames = []
    users.forEach(user=>{
        usernames.push(user.username)
    })
    res.json(usernames)
})

app.get('/',function(req,res){
    res.json(data)
})


app.get('/push/:username/:text',function(req,res){
    const username = req.params.username;
    const text = req.params.text;
    const title ="title..."
    
    pushData(username, title, text, id++);
    res.json({status:'success'})
})
    


server.listen(port, function(){
    console.log(`server listening on http://localhost:${port}`);
})