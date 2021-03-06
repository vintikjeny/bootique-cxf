package io.bootique.cxf.conf;

import org.apache.cxf.configuration.ConfiguredBeanLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class MultisourceBeanLocatorTest {

    private ConfiguredBeanLocator source1;
    private ConfiguredBeanLocator source2;
    private MultisourceBeanLocator multisourceBeanLocator;

    @Before
    public void setUp() throws Exception {
        source1 = mock(ConfiguredBeanLocator.class);
        source2 = mock(ConfiguredBeanLocator.class);

        multisourceBeanLocator = new MultisourceBeanLocator(source1, source2);
    }

    @Test(expected = Exception.class)
    public void atLeastOneSourceIsRequired() {
        new MultisourceBeanLocator();
    }

    @Test
    public void testGetBeanNamesOfType_emptySource() {

        when(source1.getBeanNamesOfType(eq(Long.class))).thenReturn(emptyList());
        when(source2.getBeanNamesOfType(eq(Long.class))).thenReturn(asList("test", "test2"));

        List<String> names = multisourceBeanLocator.getBeanNamesOfType(Long.class);

        Assert.assertArrayEquals(asList("test", "test2").toArray(), names.toArray());
    }

    @Test
    public void testGetBeanNamesOfType_nullSource() {

        when(source1.getBeanNamesOfType(eq(Long.class))).thenReturn(null);
        when(source2.getBeanNamesOfType(eq(Long.class))).thenReturn(asList("test", "test2"));

        List<String> names = multisourceBeanLocator.getBeanNamesOfType(Long.class);

        Assert.assertArrayEquals(asList("test", "test2").toArray(), names.toArray());
    }

    @Test
    public void testGetBeanNamesOfType_mergeResults() {

        when(source1.getBeanNamesOfType(eq(Long.class))).thenReturn(asList("_test", "_test2"));
        when(source2.getBeanNamesOfType(eq(Long.class))).thenReturn(asList("test", "test2"));

        List<String> names = multisourceBeanLocator.getBeanNamesOfType(Long.class);

        Assert.assertArrayEquals(asList("_test", "_test2", "test", "test2").toArray(), names.toArray());
    }


    @Test
    public void testGetBeanOfType_nullFirstSource() {
        when(source1.getBeanOfType(anyString(), eq(Long.class))).thenReturn(null);
        when(source2.getBeanOfType(anyString(), eq(Long.class))).thenReturn(3L);

        Long bean = multisourceBeanLocator.getBeanOfType("test", Long.class);

        Assert.assertEquals(3L, bean.longValue());
    }


    @Test
    public void testGetBeanOfType_nullBothSources() {
        when(source1.getBeanOfType(anyString(), eq(Long.class))).thenReturn(null);
        when(source2.getBeanOfType(anyString(), eq(Long.class))).thenReturn(null);

        Long bean = multisourceBeanLocator.getBeanOfType("test", Long.class);

        Assert.assertNull(bean);
    }

    @Test
    public void testGetBeanOfType_notNullBothSources() {
        when(source1.getBeanOfType(anyString(), eq(Long.class))).thenReturn(2L);
        when(source2.getBeanOfType(anyString(), eq(Long.class))).thenReturn(3L);

        Long bean = multisourceBeanLocator.getBeanOfType("test", Long.class);

        verifyNoMoreInteractions(source2);
        Assert.assertEquals(2L, bean.longValue());
    }


    @Test
    public void testGetBeansOfType_emptySource() {


        when(source1.getBeansOfType(eq(Long.class))).thenReturn(emptyList());
        when(source2.getBeansOfType(eq(Long.class))).thenReturn((Collection) asList(2L, 3L));

        Collection<? extends Long> beans = multisourceBeanLocator.getBeansOfType(Long.class);

        Assert.assertArrayEquals(asList(2L, 3L).toArray(), beans.toArray());
    }

    @Test
    public void testGetBeansOfType_nullSource() {


        when(source1.getBeansOfType(eq(Long.class))).thenReturn(null);
        when(source2.getBeansOfType(eq(Long.class))).thenReturn((Collection) asList(2L, 3L));

        Collection<? extends Long> beans = multisourceBeanLocator.getBeansOfType(Long.class);

        Assert.assertArrayEquals(asList(2L, 3L).toArray(), beans.toArray());
    }


    @Test
    public void testGetBeansOfType_mergeSource() {


        when(source1.getBeansOfType(eq(Long.class))).thenReturn((Collection) asList(1L, 3L));
        when(source2.getBeansOfType(eq(Long.class))).thenReturn((Collection) asList(2L, 3L));

        Collection<? extends Long> beans = multisourceBeanLocator.getBeansOfType(Long.class);

        Assert.assertArrayEquals(asList(1L, 3L, 2L, 3L).toArray(), beans.toArray());
    }

    @Test
    public void testLoadBeansOfType() {

        ConfiguredBeanLocator.BeanLoaderListener listener = mock(ConfiguredBeanLocator.BeanLoaderListener.class);
        multisourceBeanLocator.loadBeansOfType(Long.class, listener);

        verify(source1).loadBeansOfType(eq(Long.class), eq(listener));
        verify(source2).loadBeansOfType(eq(Long.class), eq(listener));
    }

    @Test
    public void testHasConfiguredPropertyValue_firstLocator() {
        when(source1.hasConfiguredPropertyValue(eq("test"), eq("testProp"), eq("val"))).thenReturn(true);
        when(source2.hasConfiguredPropertyValue(eq("test"), eq("testProp"), eq("val"))).thenReturn(false);

        verifyNoMoreInteractions(source2);
        Assert.assertTrue(multisourceBeanLocator.hasConfiguredPropertyValue("test", "testProp", "val"));
    }

    @Test
    public void testHasConfiguredPropertyValue_secondLocator() {
        when(source1.hasConfiguredPropertyValue(eq("test"), eq("testProp"), eq("val"))).thenReturn(false);
        when(source2.hasConfiguredPropertyValue(eq("test"), eq("testProp"), eq("val"))).thenReturn(true);

        Assert.assertTrue(multisourceBeanLocator.hasConfiguredPropertyValue("test", "testProp", "val"));
    }

    @Test
    public void testHasConfiguredPropertyValue_noLocator() {
        when(source1.hasConfiguredPropertyValue(eq("test"), eq("testProp"), eq("val"))).thenReturn(false);
        when(source2.hasConfiguredPropertyValue(eq("test"), eq("testProp"), eq("val"))).thenReturn(false);

        Assert.assertFalse(multisourceBeanLocator.hasConfiguredPropertyValue("test", "testProp", "val"));
    }


    @Test
    public void testHasBeanOfName_firstLocator() {
        when(source1.hasBeanOfName(eq("test"))).thenReturn(true);
        when(source2.hasBeanOfName(eq("test"))).thenReturn(false);

        verifyNoMoreInteractions(source2);
        Assert.assertTrue(multisourceBeanLocator.hasBeanOfName("test"));
    }

    @Test
    public void testHasBeanOfName_secondLocator() {
        when(source1.hasBeanOfName(eq("test"))).thenReturn(false);
        when(source2.hasBeanOfName(eq("test"))).thenReturn(true);

        Assert.assertTrue(multisourceBeanLocator.hasBeanOfName("test"));
    }

    @Test
    public void testHasBeanOfName_noLocator() {
        when(source1.hasBeanOfName(eq("test"))).thenReturn(false);
        when(source2.hasBeanOfName(eq("test"))).thenReturn(false);

        Assert.assertFalse(multisourceBeanLocator.hasBeanOfName("test"));
    }
}
