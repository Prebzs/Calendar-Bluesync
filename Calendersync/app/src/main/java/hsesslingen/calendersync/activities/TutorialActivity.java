package hsesslingen.calendersync.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import hsesslingen.calendersync.R;

public class TutorialActivity extends Activity implements Animation.AnimationListener
{
	private static final float SWIPE_DISTANCE_MIN = 120;

	private SeekBar seekBar;
	private TextView information;
	private ImageView nextButton;
	private TextView nextButtonText;
	private ViewFlipper viewFlipper;

	private float lastX;
	private float lastY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.tutorial_layout);

		seekBar =           (SeekBar) findViewById(R.id.walktrough_seekbar);
		information =       (TextView) findViewById(R.id.walktrough_text);
		nextButton =        (ImageView) findViewById(R.id.walktrough_button_background);
		nextButtonText =    (TextView) findViewById(R.id.walktrough_button);
		viewFlipper =       (ViewFlipper) findViewById(R.id.walktrough_view_flipper);

		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (viewFlipper.getDisplayedChild() < 2) {
					viewFlipper.setInAnimation(getApplicationContext(), R.anim.in_from_right);
					viewFlipper.setOutAnimation(getApplicationContext(), R.anim.out_to_left);
					viewFlipper.showNext();
					seekBar.setProgress(viewFlipper.getDisplayedChild());
				}
				else {
					finishTutorial();
				}
			}
		});

		nextButtonText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nextButton.performClick();
			}
		});

		//max = 3
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				viewFlipper.setInAnimation(null);
				viewFlipper.setOutAnimation(null);
				viewFlipper.setDisplayedChild(progress);
				changeText(viewFlipper.getDisplayedChild());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		changeText(viewFlipper.getDisplayedChild());
	}

	@Override
	public void onAnimationStart(Animation animation) {
		//nothing
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		//nothing
	}

	//handle swipeEvent
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			//onPress
			case MotionEvent.ACTION_DOWN: {
				lastX = event.getX();
				lastY = event.getY();
				break;
			}

			//onRelease
			case MotionEvent.ACTION_UP: {
				float currentX = event.getX();
				float currentY = event.getY();

				float disX;
				float disY;

				disX = currentX - lastX;
				disY = currentY - lastY;

				//compare with minimum values for swipeEvent
				if (Math.abs(disX) > Math.abs(disY) && Math.abs(disX) > SWIPE_DISTANCE_MIN) {
					//swipe to left side (next screen right)
					if (disX < 0) {
						if (viewFlipper.getDisplayedChild() == 2) {
							finishTutorial();
							break;
						}

						viewFlipper.setInAnimation(this, R.anim.in_from_right);
						viewFlipper.setOutAnimation(this, R.anim.out_to_left);

						viewFlipper.showNext();

						seekBar.setProgress(viewFlipper.getDisplayedChild());
					}
					//swipe to right side (next screen left)
					else {
						if (viewFlipper.getDisplayedChild() == 0) {
							break;
						}

						viewFlipper.setInAnimation(this, R.anim.in_from_left);
						viewFlipper.setOutAnimation(this, R.anim.out_to_right);

						viewFlipper.showPrevious();

						seekBar.setProgress(viewFlipper.getDisplayedChild());
					}
				}
				break;
			}
		}
		return false;
	}

	//change text and button after viewFlip
	private void changeText(int number) {
		switch (number) {
			case 0:
				information.setText(R.string.walkthrough_text0);
				nextButtonText.setText(R.string.walkthrough_button);
				break;
			case 1:
				information.setText(R.string.walkthrough_text1);
				nextButtonText.setText(R.string.walkthrough_button);
				break;
			case 2:
				information.setText(R.string.walkthrough_text2);
				nextButtonText.setText(R.string.walkthrough_button);
				break;
		}
	}

	//start next Activity
	private void finishTutorial()
	{
		finish();
	}
}
