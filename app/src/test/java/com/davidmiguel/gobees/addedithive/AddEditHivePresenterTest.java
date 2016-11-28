package com.davidmiguel.gobees.addedithive;

import com.davidmiguel.gobees.data.model.Hive;
import com.davidmiguel.gobees.data.model.HiveMother;
import com.davidmiguel.gobees.data.source.GoBeesDataSource;
import com.davidmiguel.gobees.data.source.cache.GoBeesRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of AddEditHivePresenter.
 */
public class AddEditHivePresenterTest {

    @Mock
    private GoBeesRepository apiariesRepository;

    @Mock
    private AddEditHiveContract.View addEditHiveView;

    private AddEditHivePresenter addEditHivePresenter;

    @Captor
    private ArgumentCaptor<GoBeesDataSource.GetHiveCallback> getHiveCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<GoBeesDataSource.GetNextHiveIdCallback> getNextHiveIdCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<GoBeesDataSource.TaskCallback> taskCallbackArgumentCaptor;

    @Before
    public void setupMocksAndView() {
        // To inject the mocks in the test the initMocks method needs to be called
        MockitoAnnotations.initMocks(this);

        // The presenter wont't update the view unless it's active
        when(addEditHiveView.isActive()).thenReturn(true);
    }

    @Test
    public void saveNewHiveToRepository_showsSuccessMessage() {
        // Get a reference to the class under test
        addEditHivePresenter =
                new AddEditHivePresenter(apiariesRepository, addEditHiveView,
                        AddEditHiveActivity.NEW_HIVE);
        // When the presenter is asked to save a hive
        addEditHivePresenter.saveHive("Hive 1", "Some notes about it....");
        // Then a new id is requested
        verify(apiariesRepository).getNextHiveId(getNextHiveIdCallbackArgumentCaptor.capture());
        getNextHiveIdCallbackArgumentCaptor.getValue().onNextHiveIdLoaded(1);
        // And the hive is saved in the repository
        verify(apiariesRepository)
                .saveHive(any(Hive.class), taskCallbackArgumentCaptor.capture());
        taskCallbackArgumentCaptor.getValue().onSuccess();
        // And the view updated
        verify(addEditHiveView).showHivesList();
    }

    @Test
    public void saveEmptyApiary_showsErrorUi() {
        // Get a reference to the class under test
        addEditHivePresenter =
                new AddEditHivePresenter(apiariesRepository, addEditHiveView,
                        AddEditHiveActivity.NEW_HIVE);
        // When the presenter is asked to save an empty hive
        addEditHivePresenter.saveHive("", "");
        // Then a new id is requested
        verify(apiariesRepository).getNextHiveId(getNextHiveIdCallbackArgumentCaptor.capture());
        getNextHiveIdCallbackArgumentCaptor.getValue().onNextHiveIdLoaded(1);
        // Then an empty hive error is shown in the UI
        verify(addEditHiveView).showEmptyHiveError();
    }

    @Test
    public void saveExistingApiaryToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test for hive with id=1
        addEditHivePresenter =
                new AddEditHivePresenter(apiariesRepository, addEditHiveView, 1);
        // When the presenter is asked to save a hive
        addEditHivePresenter.saveHive("Apiary 1", "Some more notes about it....");
        // Then a hive is saved in the repository
        verify(apiariesRepository)
                .saveHive(any(Hive.class), taskCallbackArgumentCaptor.capture());
        taskCallbackArgumentCaptor.getValue().onSuccess();
        // And the view updated
        verify(addEditHiveView).showHivesList();
    }

    @Test
    public void populateApiary_callsRepoAndUpdatesView() {
        Hive testHive = HiveMother.newDefaultHive();
        // Get a reference to the class under test
        addEditHivePresenter = new AddEditHivePresenter(
                apiariesRepository, addEditHiveView, testHive.getId());
        // When the presenter is asked to populate an existing hive
        addEditHivePresenter.populateHive();
        // Then the repository is queried and the view updated
        verify(apiariesRepository).getHive(eq(testHive.getId()),
                getHiveCallbackArgumentCaptor.capture());
        // Simulate callback
        getHiveCallbackArgumentCaptor.getValue().onHiveLoaded(testHive);
        // Verify UI has been updated
        verify(addEditHiveView).setName(testHive.getName());
        verify(addEditHiveView).setNotes(testHive.getNotes());
    }
}