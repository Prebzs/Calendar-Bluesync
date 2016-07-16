package hsesslingen.calendersync.backend;

import android.app.Activity;
import android.util.Log;

////////////////////////////////////////////////////////////////
// CalendarViewModelFactory-class to create
// and assign the CalendarViewModel
///////////////////////////////////////////////////////////////
public class CalendarViewModelFactory
{
    ////////////////////////////////////////////////////////////////
    // Members of CalendarViewModelFactory-class
    ///////////////////////////////////////////////////////////////

    private static Activity          m_activity;
    private static CalendarViewModel m_viewModel;

	////////////////////////////////////////////////////////////////
	// Public Methods of CalendarViewModelFactory-class
	///////////////////////////////////////////////////////////////

    public static void setActivity(Activity activity)
    {
        m_activity = activity;
    }

    static public CalendarViewModel GetCalendarViewModel()
    {
        if(m_activity == null)
        {
			Log.e("ViewModelFactory:", "No activity assigned to factory.");
            return null;
        }

        //create viewModel only once
        if(m_viewModel == null)
        {
			CreateCalendarViewModel();
        }

        return m_viewModel;
    }

	////////////////////////////////////////////////////////////////
	// Private Methods of CalendarViewModelFactory-class
	///////////////////////////////////////////////////////////////

	static private void CreateCalendarViewModel()
	{
		//create the CalendarModel
		CalendarModel model = new CalendarModel(m_activity);

		//create the CalendarViewModel
		m_viewModel = new CalendarViewModel(model,m_activity);
	}
}
