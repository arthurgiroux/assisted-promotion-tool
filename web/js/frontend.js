$(document).ready(function () {

	var months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "September", "November", "December"];

	var events = {
		"days_countdown" : {
			"title" : "Title days_countdown",
			"text" : "Text days_countdown",
			"icon" : "time",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_video_clip" : {
			"title" : "Title days_video_clip",
			"text" : "Text days_video_clip",
			"icon" : "facetime-video",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_press_campaign" : {
			"title" : "Title days_press_campaign",
			"text" : "Text days_press_campaign",
			"icon" : "comment",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_presale_campaign" : {
			"title" : "Title days_presale_campaign",
			"text" : "Text days_presale_campaign",
			"icon" : "calendar",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_interview" : {
			"title" : "Title days_interview",
			"text" : "Text days_interview",
			"icon" : "bullhorn",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_single_release" : {
			"title" : "Title days_single_release",
			"text" : "Text days_single_release",
			"icon" : "music",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_cd_release_show" : {
			"title" : "Title days_cd_release_show",
			"text" : "Text days_cd_release_show",
			"icon" : "eject",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_first_tweet" : {
			"title" : "Title days_first_tweet",
			"text" : "Text days_first_tweet",
			"icon" : "bullhorn",
			"color" : "#FFFFFF",
			"bgcolor" : "#00a8ff"
		},
		"days_first_fb" : {
			"title" : "Title days_first_fb",
			"text" : "Text days_first_fb",
			"icon" : "thumbs-up",
			"color" : "#FFFFFF",
			"bgcolor" : "#002ec6"
		},
		"days_announcement" : {
			"title" : "Title days_announcement",
			"text" : "Text days_announcement",
			"icon" : "bullhorn",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_album_cover" : {
			"title" : "Title days_album_cover",
			"text" : "Text days_album_cover",
			"icon" : "picture",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_teaser" : {
			"title" : "Title days_teaser",
			"text" : "Text days_teaser",
			"icon" : "film",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"release_date" : {
			"title" : "Release date",
			"text" : "Text blabla",
			"icon" : "ok",
			"color" : "#FFFFFF",
			"bgcolor" : "#23d300"
		}
	};

	var createTimelineElement = function (data, inverted) {
		var event = events[data.event];

		inverted = typeof inverted !== 'undefined' ? inverted : false;
		var element = inverted ? $('<li class="timeline-inverted"></li>') : $('<li></li>');
		var panel = $('<div class="timeline-panel">');
		var panelHeading = $('<div class="timeline-heading"></div>');
		var panelBody = $('<div class="timeline-body"></div>');

		var timelineBadge = $('<div class="timeline-badge"></div>');
		timelineBadge.css('color', event.color);
		timelineBadge.css('background-color', event.bgcolor);
		timelineBadge.append('<i class="glyphicon glyphicon-' + event.icon + '"></i>');
		element.append(timelineBadge);
		panelHeading.append('<h4 class="timeline-title">' + event.title + '</h4>');

		var today = new Date();

		var dateString = months[data.date.getMonth()] + " " + data.date.getDate() + ", " + data.date.getFullYear();
		panelHeading.append('<p><small class="text-muted"><i class="glyphicon glyphicon-time"></i> on ' + dateString + '</small></p>');
		if (data.date < today) {
			panelHeading.css("color", "red");
		}
		panelBody.append('<p>' + event.text + '</p>');

		element.append(panel);
		panel.append(panelHeading);
		panel.append(panelBody);

		return element;
	};

	var getTodayDateValue = function () {
		var date = new Date();

		// Format YYYY-MM-DD
		var s = date.getFullYear() + "-";

		// Months in [0-11]
		if (date.getMonth() < 9) {
			s += "0";
		}
		s += (date.getMonth() + 1) + "-";

		// Days in [1-31]
		if (date.getDate() < 10) {
			s += "0";
		}
		s += date.getDate();

		return s;
	};

	$("#releaseDate").val(getTodayDateValue());

	$('#beginButton').click(function () {
		$('#formContainer').fadeIn();
		$.scrollTo($('#formContainer'), 800);
	});
	
	$('#recommandationForm').submit(function (e) {
		e.preventDefault();

		// Some integrity checks are missing here...

		var releaseDate = new Date($("#releaseDate").val());

		$.ajax({
			url: 'http://icdatasrv4.epfl.ch:8000/recommend',
			type: 'GET',
			crossDomain: true,
			data: $(this).serialize(),
			dataType: 'jsonp'
		}).done(function (data) {
			if (data.error) {
				alert(data.error);
				return;
			}

			$('#statsContent').empty();

			var statsText = "This timeline was computed by aggregating the promotion timeline of " + data.stats.artists_count + " artists (avg. similarity : " + data.stats.average_sim + "). <br />";
			statsText += "Average similarity over all the artists of our database : " + data.stats.average_sim_overall + "<br />";
			statsText += "Artist best match : " + data.stats.best_match + "<br />";
			$('#statsContent').html(statsText);

			$('#timelineContent').empty();

			// Constructs the timeline
			var oldestDate = releaseDate;
			for (var i in data.results) {
				var obj = data.results[i];
				var d = new Date(releaseDate);
				d.setDate(d.getDate() - Math.round(obj.days));
				
				// Find oldest date
				if (d < oldestDate) {
					oldestDate = d;
				}

				$('#timelineContent').append(createTimelineElement({
					event : obj.event,
					date : d
				}, i%2 == 1));
			}

			var today = new Date();
			if (oldestDate < today) {
				var warning = $('<div class="alert alert-danger"><b>Warning</b> You should release your album later, some recommended events are in the past !</div>')
				$('#statsContent').append(warning);
			}

			$('#timelineContent').append(createTimelineElement({
				event : "release_date",
				date : releaseDate
			}, data.results.length%2 == 1));

			// Displays the timeline
			$('#timelineContainer').fadeIn();
			$.scrollTo($('#timelineContainer'), 800);
		}).fail(function (data) {
			console.log(data);
			alert("There was a problem while retrieving the data.");
		});
	});
});