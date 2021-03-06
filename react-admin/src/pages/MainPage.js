import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Redirect, Route, Switch, withRouter} from "react-router-dom";
import {bindActionCreators} from "redux";
import './HomePage.css'
import {Layout, message} from 'antd';
import AppHeader from "../components/AppHeader";
import AppFooter from "../components/AppFooter";
import AppLeftMenu from "../components/AppLeftMenu";
import AppLogo from "../components/AppLogo";
import AppBreadcrumb from "../components/AppBreadcrumb";
import HomeContent from "./contents/HomeContent";

import ProductAddContent from "./contents/shop/ProductAddContent";
import ProductEditContent from "./contents/shop/ProductEditContent";
import CategoryEditContent from "./contents/shop/CategoryEditContent";
import OrderListContent from "./contents/order/OrderListContent";
import StatisOverviewContent from "./contents/statistic/StatisOverviewContent";
import AdvertiseContent from "./contents/marketing/AdvertiseContent";
import MerchantContent from "./contents/admin/MerchantContent";
import UserContent from "./contents/admin/UserContent";
import CategoryReducer from "../data/redux/reducers/shop/CategoryReducer";
import {
    onCategoryRefreshFailed,
    onCategoryRefreshRetry,
    refreshCategoryList
} from "../data/redux/reducers/shop/CategoryActionCreator";
import {httpCategoryList} from "../data/http/HttpRequest";
import {RetryResult} from "../components/RetryResult";
import CenteredLoading from "../components/CentredLoading";

const {Header, Content, Footer, Sider} = Layout;

class MainPage extends Component {
    state = {
        collapsed: false
    };

    componentDidMount() {
        this.loadingData();
    }
    onRetryClicked = (e) => {
        this.props.onCategoryRefreshRetry();
        this.loadingData();
    }

    loadingData = (e) => {
        httpCategoryList((response)=>{
            this.props.refreshCategoryList({data:response.data});
        },(failed)=>{
            message.error(failed.message);
            this.props.onCategoryRefreshFailed();
        });
    }




    render() {
        if(this.props.contentLoading)
            return <CenteredLoading/>
        if(!this.props.success){
            return <RetryResult onRetryClicked={this.onRetryClicked} />
        }
        return (
            <Layout style={{minHeight: '100vh'}}>
                <Sider
                    breakpoint="lg"
                    collapsedWidth="0"
                    onBreakpoint={broken => {
                        console.log(broken);
                    }}
                    onCollapse={(collapsed, type) => {
                        console.log(collapsed, type);
                    }}
                >
                    <AppLogo/>
                    <AppLeftMenu/>

                </Sider>

                <Layout>
                    <Header style={{padding: '0 16px', backgroundColor: '#fff'}}>
                        <AppHeader/>
                    </Header>
                    <Content style={{margin: '16px 16px 0'}}>
                        <AppBreadcrumb/>
                        <div debug={'content'} style={{padding: 24, minHeight: 720, backgroundColor: '#fff'}}>
                                <Switch>
                                    <Route path="/shop/productAdd" exact={true}>
                                        <ProductAddContent/>
                                    </Route>
                                    <Route path="/shop/productEdit" exact={true}>
                                        <ProductEditContent/>
                                    </Route>
                                    <Route path="/shop/categoryEdit" exact={true}>
                                        <CategoryEditContent/>
                                    </Route>
                                    <Route path="/order/orderEdit" exact={true}>
                                        <OrderListContent/>
                                    </Route>
                                    <Route path="/statis/overview" exact={true}>
                                        <StatisOverviewContent/>
                                    </Route>
                                    <Route path="/marketing/ad" exact={true}>
                                        <AdvertiseContent/>
                                    </Route>
                                    <Route path="/admin/merchant" exact={true}>
                                        <MerchantContent/>
                                    </Route>
                                    <Route path="/admin/user" exact={true}>
                                        <UserContent/>
                                    </Route>
                                    <Route path="/"  exact={true}>
                                        <HomeContent/>
                                    </Route>

                                    <Redirect from='*' to='/notFound'/>
                                </Switch>

                        </div>
                    </Content>
                    <Footer>
                        <AppFooter/>
                    </Footer>
                </Layout>
            </Layout>
        );
    }
}

function mapState(state) {
    console.log(state)
    return {...state.categoryReducer};
}

const mapDispatch = (dispatch, ownProps) => {
    return bindActionCreators({
        refreshCategoryList,
        onCategoryRefreshFailed,
        onCategoryRefreshRetry
    }, dispatch);
}

export default withRouter(connect(mapState, mapDispatch)(MainPage));