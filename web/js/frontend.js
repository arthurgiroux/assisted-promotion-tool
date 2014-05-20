$(document).ready(function () {

	var months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "September", "November", "December"];

	var events = {
		"days_countdown" : {
			"title" : "Countdown before the release",
			"text" : "<blockquote><p>Only 15 days before our new album ! Stay tuned !</p></blockquote>",
			"icon" : "time",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_video_clip" : {
			"title" : "Video clip",
			"text" : "<blockquote><p>Our new video clip for our latest album is up on Youtube.</p></blockquote>",
			"icon" : "facetime-video",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_press_campaign" : {
			"title" : "Press campaign",
			"text" : "<blockquote><p>Check us in this new press article in the Guardian.</p></blockquote>",
			"icon" : "comment",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_presale_campaign" : {
			"title" : "Presale campaign",
			"text" : "<blockquote><p>Preorder our new album now for only 10.- !</p></blockquote>",
			"icon" : "calendar",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_interview" : {
			"title" : "Interview",
			"text" : "<blockquote><p>Check our interview tonight on the Late Night Show !</p></blockquote>",
			"icon" : "bullhorn",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_single_release" : {
			"title" : "Release of a single",
			"text" : "<blockquote><p>We released the single &laquo; Big Data &raquo; of the album &laquo; Pretty big things &raquo; Check it out !</p></blockquote>",
			"icon" : "music",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_cd_release_show" : {
			"title" : "CD release show",
			"text" : "<blockquote><p>Tonight to celebrate the release of our CD we are doing a show at Hyde Park</p></blockquote>",
			"icon" : "eject",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_first_tweet" : {
			"title" : "First tweet about the album",
			"text" : "<blockquote><p>We are working very hard on our new album.</p></blockquote>",
			"icon" : "bullhorn",
			"color" : "#FFFFFF",
			"bgcolor" : "#00a8ff"
		},
		"days_first_fb" : {
			"title" : "First facebook post about the album",
			"text" : "<blockquote><p>Stay tuned for our new album very soon !</p></blockquote>",
			"icon" : "thumbs-up",
			"color" : "#FFFFFF",
			"bgcolor" : "#002ec6"
		},
		"days_announcement" : {
			"title" : "Announcement about the album",
			"text" : "<blockquote><p>We are proud to announce the making of our new album.</p></blockquote>",
			"icon" : "bullhorn",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_album_cover" : {
			"title" : "Album cover artwork",
			"text" : "<blockquote><p>Check our amazing cover artwork made by the great artist Lolita Mujer.</p></blockquote>",
			"icon" : "picture",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"days_teaser" : {
			"title" : "Teaser",
			"text" : "<blockquote><p>Today we released a teaser for our new album, check it out !</p></blockquote>",
			"icon" : "film",
			"color" : "#FFFFFF",
			"bgcolor" : "#000000"
		},
		"release_date" : {
			"title" : "Release date",
			"text" : "<blockquote><p>Our album &laquo; Big Data &raquo; will be released at the end of this month !</p></blockquote>",
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


			var statsText = "<p>This timeline was computed by aggregating the promotion timeline of <b>" + data.stats.artists_count + "</b> artists<br />"; //(avg. similarity : " + data.stats.average_sim + "). <br />";
			statsText += "Average similarity over all the artists of our database : <b>" + data.stats.average_sim_overall + "</b><br />";
			statsText += "Artist best match : <h4>&laquo;" + data.stats.best_match + "&raquo;</h4><br /></p>";
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