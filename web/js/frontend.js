$(document).ready(function () {
	$('#beginButton').click(function () {
		$('#formContainer').fadeIn();
		$.scrollTo($('#formContainer'), 800);
	});
	
	$('#recommandationForm').submit(function (e) {
		e.preventDefault();

		$.ajax({
			url: 'dunno',
			type: 'POST',
			data: $(this).serialize(),
			dataType: 'json'
		}).done(function (data) {

			// Displays the timeline
			$('#timelineContainer').fadeIn();
			$.scrollTo($('#timelineContainer'), 800);
		}).fail(function (data) {
			alert("There was a problem while retrieving the data.");
		});
	});
});