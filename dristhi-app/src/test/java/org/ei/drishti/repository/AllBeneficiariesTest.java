package org.ei.drishti.repository;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.ei.drishti.domain.Mother;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class AllBeneficiariesTest {
    @Mock
    private MotherRepository motherRepository;
    @Mock
    private ChildRepository childRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private TimelineEventRepository timelineEventRepository;

    private AllBeneficiaries allBeneficiaries;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        allBeneficiaries = new AllBeneficiaries(motherRepository, childRepository, alertRepository, timelineEventRepository);
    }

    @Test
    public void shouldDeleteTimelineEventsAndAlertsWhileClosingMother() throws Exception {
        allBeneficiaries.closeMother("entity id 1");

        verify(alertRepository).deleteAllAlertsForEntity("entity id 1");
        verify(timelineEventRepository).deleteAllTimelineEventsForEntity("entity id 1");
    }

    @Test
    public void shouldDeleteTimelineEventsAndAlertsForAllMothersWhenECIsClosed() throws Exception {
        when(motherRepository.findAllCasesForEC("ec id 1"))
                .thenReturn(asList(new Mother("mother id 1", "ec id 1", "12345", "2012-12-12"), new Mother("mother id 2", "ec id 2", "123456", "2012-12-10")));

        allBeneficiaries.closeAllMothersForEC("ec id 1");

        verify(alertRepository).deleteAllAlertsForEntity("mother id 1");
        verify(alertRepository).deleteAllAlertsForEntity("mother id 2");
        verify(timelineEventRepository).deleteAllTimelineEventsForEntity("mother id 1");
        verify(timelineEventRepository).deleteAllTimelineEventsForEntity("mother id 2");
        verify(motherRepository).close("mother id 1");
        verify(motherRepository).close("mother id 2");
    }

    @Test
    public void shouldNotFailClosingMotherWhenECIsClosedAndDoesNotHaveAnyMothers() throws Exception {
        when(motherRepository.findAllCasesForEC("ec id 1")).thenReturn(null);
        when(motherRepository.findAllCasesForEC("ec id 1")).thenReturn(Collections.<Mother>emptyList());

        allBeneficiaries.closeAllMothersForEC("ec id 1");

        verifyZeroInteractions(alertRepository);
        verifyZeroInteractions(timelineEventRepository);
        verify(motherRepository, times(0)).close(any(String.class));
    }
}