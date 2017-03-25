function pollServices(aloo) {
  $.ajax({
    url: '/services',
    type: 'get',
    success: function(r) {
      console.log(r);
      r = r
        .map(task =>
            `<div data-task-id=${task.id}>
              <div class="scrum_task blocker">
                <h3 class="scrum_task_title"><a href="#task_info" data-uk-modal="{ center:true }">${task.room_id}</a></h3>
                <p class="scrum_task_description">${task.query}</p>
                <p class="scrum_task_info"><span class="uk-text-muted">At:</span> <a href="#">${task.created_tx}</a></p>
              </div>
            </div>`
        )
        .join('');

      $('#scrum_column_todo').empty().append(r);
    }
  });
}

var servicePollTimer = setInterval(pollServices, 2500);
