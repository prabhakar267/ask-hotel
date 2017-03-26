function makeid()
{
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for( var i=0; i < 10; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}

function pollServices() {
  $.ajax({
    url: '/services',
    type: 'get',
    data: {t_id: makeid()},
    success: function(r) {
      $('.scrum_column>div').empty();

      r.forEach(task => {
        // task.created_tx = Number(task.created_tx.split('.')[0]);
        var date = new Date(task.created_tx);
        // Hours part from the timestamp
        var hours = date.getHours();
        // Minutes part from the timestamp
        var minutes = "0" + date.getMinutes();
        // Seconds part from the timestamp
        var seconds = "0" + date.getSeconds();

        // Will display time in 10:30:23 format
        var formattedTime = hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);

        var template = `<div data-task-id=${task.id}>
          <div class="scrum_task blocker">
            <h3 class="scrum_task_title">Room Number : ${task.room_id}</h3>
            <p class="scrum_task_description">${task.query}</p>
            <p class="scrum_task_info"><span class="uk-text-muted"><i class="material-icons">schedule</i></span> ${formattedTime}</p>
          </div>
        </div>`;
        switch(task.mode) {
          case 2:
            $('#scrum_column_todo').append(template);
            break;
          case 3:
            $('#scrum_column_restaurant').append(template);
            break;
          case 4:
            $('#scrum_column_housekeeping').append(template);
          case 5:
            $('#scrum_column_done').append(template);
        }
      });
    }
  });
}


$(function () {
    altair_scrum_board.init(), altair_scrum_board.draggable_tasks()
}), altair_scrum_board = {
    init: function () {
        var a = $("#scrum_board"),
            r = a.children("div").width(),
            o = a.children("div").length;
        a.width(r * o)
    },
    draggable_tasks: function () {

        var serviceRequests = setInterval(pollServices, 3000);

        for (var a = dragula($(".scrum_column > div").toArray()), r = a.containers, o = r.length, l = 0; l < o; l++) $(r[l]).addClass("dragula dragula-vertical");
        a.on("drop", function (a, r, o, l) {
            // console.log(a), console.log(r), console.log(o)
            r = r.id.split('_');
            r = r[r.length - 1];

            a = Number(a.attributes['data-task-id'].value);

            var mode;

            switch(r) {
              case 'restaurant':
                mode = 3;
                break;
              case 'todo':
                mode = 2;
                break;
              case 'housekeeping':
                mode = 4;
                break;
              case 'done':
                mode = 5;
                break;
            }

            $.ajax({
              url: '/update/services',
              type: 'get',
              data: {id: a, mode: mode},
              success: function(r) {
                if (!serviceRequests) {
                  pollServices()
                  serviceRequests = setInterval(pollServices, 3000);
                }
              }
            });
        })

        a.on("drag", function (a, r, o, l) {
          console.log("dragging")
            // console.log(a), console.log(r), console.log(o)
            // console.log(serviceRequests);
            clearInterval(serviceRequests);
            serviceRequests = null;
        })
    }
};
