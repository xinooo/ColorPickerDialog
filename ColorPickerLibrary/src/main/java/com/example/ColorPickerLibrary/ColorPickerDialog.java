package com.example.ColorPickerLibrary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ColorPickerDialog {
	public interface OnColorPickerListener {
		void onCancel(ColorPickerDialog dialog);

		void onOk(ColorPickerDialog dialog, String color);
	}

	private Toast toast;
	private final AlertDialog dialog;
	private final boolean supportsAlpha;
	final OnColorPickerListener listener;
    private final View viewHue;
    private final ColorPickerSquare viewSatVal;
    private final ImageView viewCursor;
    private final ImageView viewAlphaCursor;
    private final View viewOldColor;
    private final View viewNewColor;
    private final View viewAlphaOverlay;
    private final View viewAlpha;
    private final ImageView viewTarget;
    private final ImageView viewAlphaCheckered;
    private final ViewGroup viewContainer;
    private final TextView cancel;
    private final TextView enter;
    private final EditText tv_color;
	final float[] currentColorHsv = new float[3];
	int alpha;


	public ColorPickerDialog(final Context context, int color, OnColorPickerListener listener) {
		this(context, color, false, listener);
	}


	@SuppressLint("ClickableViewAccessibility")
    public ColorPickerDialog(final Context context, int color, boolean supportsAlpha, OnColorPickerListener listener) {
		this.supportsAlpha = supportsAlpha;
		this.listener = listener;
		toast = Toast.makeText(context, "", Toast.LENGTH_LONG);

		if (!supportsAlpha) { // remove alpha if not supported
			color = color | 0xff000000;
		}

		Color.colorToHSV(color, currentColorHsv);
		alpha = Color.alpha(color);

		final View view = LayoutInflater.from(context).inflate(R.layout.colorpicker_dialog, null);
		viewHue = view.findViewById(R.id.viewHue);
		viewSatVal = (ColorPickerSquare) view.findViewById(R.id.viewSatBri);
		viewCursor = (ImageView) view.findViewById(R.id.cursor);
		viewOldColor = view.findViewById(R.id.oldColor);
		viewNewColor = view.findViewById(R.id.newColor);
		viewTarget = (ImageView) view.findViewById(R.id.target);
		viewContainer = (ViewGroup) view.findViewById(R.id.viewContainer);
		viewAlpha = (ViewGroup) view.findViewById(R.id.alpha);
		viewAlphaOverlay = view.findViewById(R.id.overlay);
		viewAlphaCursor = (ImageView) view.findViewById(R.id.alphaCursor);
		viewAlphaCheckered = (ImageView) view.findViewById(R.id.alphaCheckered);
		cancel = (TextView)view.findViewById(R.id.cancel);
        enter = (TextView)view.findViewById(R.id.enter);
        tv_color = (EditText) view.findViewById(R.id.tv_color);

        { // hide/show alpha
			viewAlphaOverlay.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
			viewAlphaCursor.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
			viewAlphaCheckered.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
		}

		viewSatVal.setHue(getHue());
		viewOldColor.setBackgroundColor(color);
		viewNewColor.setBackgroundColor(color);
        tv_color.setText(String.format("#%08x", color));

		viewHue.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
				|| event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_UP) {

					float y = event.getY();
					if (y < 0.f) y = 0.f;
					if (y > viewHue.getMeasuredHeight()) {
						y = viewHue.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
					}
					float hue = 360.f - 360.f / viewHue.getMeasuredHeight() * y;
					if (hue == 360.f) hue = 0.f;
					setHue(hue);

					// update view
					viewSatVal.setHue(getHue());
					moveCursor();
					viewNewColor.setBackgroundColor(getColor());
                    tv_color.setText(String.format("#%08x", getColor()));
					updateAlphaView();
					return true;
				}
				return false;
			}
		});

		/*Portrait*/
//		if (supportsAlpha) viewAlphaCheckered.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if ((event.getAction() == MotionEvent.ACTION_MOVE)
//				|| (event.getAction() == MotionEvent.ACTION_DOWN)
//				|| (event.getAction() == MotionEvent.ACTION_UP)) {
//
//					float y = event.getY();
//					if (y < 0.f) {
//						y = 0.f;
//					}
//					if (y > viewAlphaCheckered.getMeasuredHeight()) {
//						y = viewAlphaCheckered.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
//					}
//					final int a = Math.round(255.f - ((255.f / viewAlphaCheckered.getMeasuredHeight()) * y));
//					ColorPickerDialog.this.setAlpha(a);
//
//					// update view
//					moveAlphaCursor();
//					int col = ColorPickerDialog.this.getColor();
//					int c = a << 24 | col & 0x00ffffff;
//					viewNewColor.setBackgroundColor(c);
//                    tv_color.setText(String.format("#%08x", c));
//					return true;
//				}
//				return false;
//			}
//		});

		/*land*/
		if (supportsAlpha) viewAlphaCheckered.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_MOVE)
						|| (event.getAction() == MotionEvent.ACTION_DOWN)
						|| (event.getAction() == MotionEvent.ACTION_UP)) {

					float x = event.getX();
					if (x < 0.f) {
						x = 0.f;
					}
					if (x > viewAlphaCheckered.getMeasuredWidth()) {
						x = viewAlphaCheckered.getMeasuredWidth() - 0.001f;
					}
					final int a = Math.round(255.f - ((255.f / viewAlphaCheckered.getMeasuredWidth()) * x));
					ColorPickerDialog.this.setAlpha(a);

					// update view
					moveAlphaCursor_land();
					final int d = Math.round(((255.f / viewAlphaCheckered.getMeasuredWidth()) * x) - 256.f);
					int col = ColorPickerDialog.this.getColor();
					int c = d << 24 | col & 0x00ffffff;
					viewNewColor.setBackgroundColor(c);
					tv_color.setText(String.format("#%08x", c));
					return true;
				}
				return false;
			}
		});
		viewSatVal.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
				|| event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_UP) {

					float x = event.getX(); // touch event are in dp units.
					float y = event.getY();

					if (x < 0.f) x = 0.f;
					if (x > viewSatVal.getMeasuredWidth()) x = viewSatVal.getMeasuredWidth();
					if (y < 0.f) y = 0.f;
					if (y > viewSatVal.getMeasuredHeight()) y = viewSatVal.getMeasuredHeight();

					setSat(1.f / viewSatVal.getMeasuredWidth() * x);
					setVal(1.f - (1.f / viewSatVal.getMeasuredHeight() * y));

					// update view
					moveTarget();
					viewNewColor.setBackgroundColor(getColor());
                    tv_color.setText(String.format("#%08x", getColor()));
					updateAlphaView();
					return true;
				}
				return false;
			}
		});

		dialog = new AlertDialog.Builder(context, R.style.dialog_color_picker)
                .setCancelable(false)
		        .create();
		// kill all padding from the dialog window
		dialog.setView(view, 0, 0, 0, 0);

		// move cursor & target on first draw
		ViewTreeObserver vto = view.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				moveCursor();
				if (ColorPickerDialog.this.supportsAlpha) moveAlphaCursor_land();
				moveTarget();
				if (ColorPickerDialog.this.supportsAlpha) updateAlphaView();
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});

		enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ColorPickerDialog.this.listener != null) {
                	if (tv_color.getText().toString().length() >= 9){
						ColorPickerDialog.this.listener.onOk(ColorPickerDialog.this, tv_color.getText().toString());
						dialog.dismiss();
					}else {
						toast.setText(context.getString(R.string.input_error));
						toast.setDuration(Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				}
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ColorPickerDialog.this.listener != null) {
					ColorPickerDialog.this.listener.onCancel(ColorPickerDialog.this);
                    dialog.dismiss();
				}
            }
        });

        tv_color.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
				int newcolor;
				if (editable.length()>=9){
					newcolor = Color.parseColor(editable.toString());
					Color.colorToHSV(newcolor, currentColorHsv);
					alpha = Color.alpha(newcolor);
					viewSatVal.setHue(getHue());
					viewNewColor.setBackgroundColor(newcolor);
					moveCursor();
					if (ColorPickerDialog.this.supportsAlpha) moveAlphaCursor_land();
					moveTarget();
					if (ColorPickerDialog.this.supportsAlpha) updateAlphaView();
				}
            }
        });
	}

	protected void moveCursor() {
		float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360.f);
		if (y == viewHue.getMeasuredHeight()) y = 0.f;
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
		viewCursor.setLayoutParams(layoutParams);
	}

	protected void moveTarget() {
		float x = getSat() * viewSatVal.getMeasuredWidth();
		float y = (1.f - getVal()) * viewSatVal.getMeasuredHeight();
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
		layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
		viewTarget.setLayoutParams(layoutParams);
	}

	protected void moveAlphaCursor_Portrait() {
		final int measuredHeight = this.viewAlphaCheckered.getMeasuredHeight();
		float y = measuredHeight - ((this.getAlpha() * measuredHeight) / 255.f);
		final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.viewAlphaCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (this.viewAlphaCheckered.getLeft() - Math.floor(this.viewAlphaCursor.getMeasuredWidth() / 2) - this.viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) ((this.viewAlphaCheckered.getTop() + y) - Math.floor(this.viewAlphaCursor.getMeasuredHeight() / 2) - this.viewContainer.getPaddingTop());

		this.viewAlphaCursor.setLayoutParams(layoutParams);
	}

	protected void moveAlphaCursor_land() {
		final int measuredWidth = this.viewAlphaCheckered.getMeasuredWidth();
		float x = 0 + ((this.getAlpha() * measuredWidth) / 255.f);
		final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.viewAlphaCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (x - Math.floor(this.viewAlphaCursor.getMeasuredWidth() / 2));
		layoutParams.topMargin = (int) (Math.floor(this.viewSatVal.getMeasuredHeight()) + this.viewAlpha.getPaddingTop()-(this.viewAlphaCursor.getMeasuredHeight()-this.viewAlphaCheckered.getMeasuredHeight())/2);

		this.viewAlphaCursor.setLayoutParams(layoutParams);
	}

	private int getColor() {
		final int argb = Color.HSVToColor(currentColorHsv);
		return alpha << 24 | (argb & 0x00ffffff);
	}

	private float getHue() {
		return currentColorHsv[0];
	}

	private float getAlpha() {
		return this.alpha;
	}

	private float getSat() {
		return currentColorHsv[1];
	}

	private float getVal() {
		return currentColorHsv[2];
	}

	private void setHue(float hue) {
		currentColorHsv[0] = hue;
	}

	private void setSat(float sat) {
		currentColorHsv[1] = sat;
	}

	private void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	private void setVal(float val) {
		currentColorHsv[2] = val;
	}

	public void show() {
		dialog.show();
	}

	public AlertDialog getDialog() {
		return dialog;
	}

	private void updateAlphaView() {
		final GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[] {
		Color.HSVToColor(currentColorHsv), 0x0
		});
		viewAlphaOverlay.setBackgroundDrawable(gd);
	}
}
