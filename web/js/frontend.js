$(document).ready(function () {

	var months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "September", "November", "December"];

	var events = {
		"days_countdown" : {
			"title" : "Title days_countdown",
			"text" : "Text days_countdown"
		},
		"days_video_clip" : {
			"title" : "Title days_video_clip",
			"text" : "Text days_video_clip"
		},
		"days_press_campaign" : {
			"title" : "Title days_press_campaign",
			"text" : "Text days_press_campaign"
		},
		"days_presale_campaign" : {
			"title" : "Title days_presale_campaign",
			"text" : "Text days_presale_campaign"
		},
		"days_interview" : {
			"title" : "Title days_interview",
			"text" : "Text days_interview"
		},
		"days_single_release" : {
			"title" : "Title days_single_release",
			"text" : "Text days_single_release"
		}
	};

	var createTimelineElement = function (data, inverted) {
		inverted = typeof inverted !== 'undefined' ? inverted : false;
		var element = inverted ? $('<li class="timeline-inverted"></li>') : $('<li></li>');
		var panel = $('<div class="timeline-panel">');
		var panelHeading = $('<div class="timeline-heading"></div>');
		var panelBody = $('<div class="timeline-body"></div>');

		element.append('<div class="timeline-badge"><i class="glyphicon glyphicon-bullhorn"></i></div>');
		panelHeading.append('<h4 class="timeline-title">' + data.title + '</h4>');
		panelHeading.append('<p><small class="text-muted"><i class="glyphicon glyphicon-time"></i> on ' + data.date + '</small></p>');
		panelBody.append('<p>' + data.message + '</p>');

		element.append(panel);
		panel.append(panelHeading);
		panel.append(panelBody);

		return element;
	};

	$("#releaseDate").val(new Date().toJSON().slice(0,10));

	$('#beginButton').click(function () {
		$('#formContainer').fadeIn();
		$.scrollTo($('#formContainer'), 800);
	});
	
	$('#recommandationForm').submit(function (e) {
		e.preventDefault();

		// Some integrity checks are missing here...

		var releaseDate = new Date($("#releaseDate").val());

		$('#timelineContent').append(createTimelineElement({
			title : "Release date",
			message : "Test",
			date : months[releaseDate.getMonth()+1] + " " + releaseDate.getDate()
		}));

		$.ajax({
			url: 'data.json',
			type: 'GET',
			crossDomain: true,
			//data: $(this).serialize(),
			dataType: 'json'
		}).done(function (data) {
			// Constructs the timeline
			for (var i in data) {
				var obj = data[i];
				var d = new Date(releaseDate);
				d.setDate(d.getDate() - Math.round(obj.days));
				$('#timelineContent').append(createTimelineElement({
					title : events[obj.event].title,
					message : events[obj.event].text,
					date : months[d.getMonth()+1] + " " + d.getDate()
				}, i%2 == 0));
			}

			// Displays the timeline
			$('#timelineContainer').fadeIn();
			$.scrollTo($('#timelineContainer'), 800);
		}).fail(function (data) {
			console.log(data);
			alert("There was a problem while retrieving the data.");
		});
	});
});