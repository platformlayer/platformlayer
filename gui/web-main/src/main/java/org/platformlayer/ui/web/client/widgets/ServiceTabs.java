package org.platformlayer.ui.web.client.widgets;

import org.platformlayer.service.aptcache.client.ServicePanel;
import org.platformlayer.service.dns.client.DnsServicePanel;
import org.platformlayer.service.dnsresolver.client.DnsResolverServicePanel;
import org.platformlayer.service.openldap.client.LdapProviderPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class ServiceTabs extends Composite {

    interface Binder extends UiBinder<Widget, ServiceTabs> {
    }

    interface Style extends CssResource {
    }

    @UiField
    TabLayoutPanel tabs;

    public ServiceTabs() {
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));

        buildTabs();

        tabs.selectTab(0);
    }

    private void buildTabs() {
        {
            LazyPanel lazyPanel = new LazyPanel() {
                @Override
                protected Widget createWidget() {
                    return new DnsServicePanel();
                }
            };
            Label label = new Label("DNS");
            tabs.add(lazyPanel, label);
        }

        {
            LazyPanel lazyPanel = new LazyPanel() {
                @Override
                protected Widget createWidget() {
                    return new DnsResolverServicePanel();
                }
            };
            Label label = new Label("DNS Resolvers");
            tabs.add(lazyPanel, label);
        }

        {
            LazyPanel lazyPanel = new LazyPanel() {
                @Override
                protected Widget createWidget() {
                    return new ServicePanel();
                }
            };
            Label label = new Label("APT Cache");
            tabs.add(lazyPanel, label);
        }

        {
            LazyPanel lazyPanel = new LazyPanel() {
                @Override
                protected Widget createWidget() {
                    return new LdapProviderPanel();
                }
            };
            Label label = new Label("LDAP");
            tabs.add(lazyPanel, label);
        }
    }

    // private void fetch() {
    // requestFactory.serviceInfoRequest().findAll(/* start, numRows, filter */).fire(new Receiver<List<ServiceInfoProxy>>() {
    // @Override
    // public void onSuccess(List<ServiceInfoProxy> response) {
    // for (final ServiceInfoProxy service : response) {
    // LazyPanel lazyPanel = new LazyPanel() {
    //
    // @Override
    // protected Widget createWidget() {
    // return new ServiceWidget(service);
    // }
    // };
    // Label label = new Label(service.getDescription());
    // tabs.add(label, lazyPanel);
    // }
    // }
    // });
    // }
}
