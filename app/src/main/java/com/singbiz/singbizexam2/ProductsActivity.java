package com.singbiz.singbizexam2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import net.frakbot.jumpingbeans.JumpingBeans;

import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsActivity extends AppCompatActivity {

    private static boolean firstTime = true;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private MenuItem searchItem;
    private MenuItem checkoutItem;
    private MenuItem deleteItem;
    private int oldOrientation;
    private FloatingActionButton buttonUp;
    private PlaceholderFragment previousFragment;
    private Boolean[] checkoutPreviousState;
    private String currentSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        currentSearch = null;
        previousFragment = null;
        searchItem = null;
        checkoutItem = null;
        deleteItem = null;
        checkoutPreviousState = new Boolean[]{null, null};

        oldOrientation = getRequestedOrientation();

        buttonUp = (FloatingActionButton) findViewById(R.id.fab);
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaceholderFragment fragment = PlaceholderFragment.entities.get(mViewPager.getCurrentItem());
                if(fragment != null)
                    fragment.scrollUp();
            }
        });



    }

    /*@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final SingBizDatabase db = new SingBizDatabase(this);
        //db.onUpgrade(db.getWritableDatabase(), 1, 2); // for debug, later delete
        db.close();
        ////
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        populateContext();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_products, menu);
        checkoutItem = menu.findItem(R.id.action_checkout);
        searchItem = menu.findItem(R.id.search);
        deleteItem = menu.findItem(R.id.action_delete);

        final SearchView search_view = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (search_view != null) {
            search_view.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            search_view.setIconifiedByDefault(true);
            search_view.setQueryHint(getResources().getString(R.string.search_title));
            search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    query = query.trim();
                    if (query.length() == 0) {
                        Toast.makeText(ProductsActivity.this, "Please enter search words",
                                Toast.LENGTH_LONG).show();
                        return true;
                    }

                    currentSearch = query;
                    PlaceholderFragment search_fragment = PlaceholderFragment.entities.get(mViewPager.getCurrentItem());
                    if (search_fragment != null)
                        search_fragment.search(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            ImageView closeButton = (ImageView)search_view.findViewById(R.id.search_close_btn);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText et = (EditText) findViewById(R.id.search_src_text);

                    //Clear the text from EditText view
                    et.setText("");

                    //Clear query
                    search_view.setQuery("", false);

                    currentSearch = null;
                    PlaceholderFragment search_fragment = PlaceholderFragment.entities.get(mViewPager.getCurrentItem());
                    if (search_fragment != null)
                        search_fragment.search(null);
                }
            });


            MenuItemCompat.setOnActionExpandListener(searchItem,
                    new MenuItemCompat.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem menuItem) {
                            // Return true to allow the action view to expand

                            return true;
                        }
                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                            // When the action view is collapsed, reset the query
                            currentSearch = null;
                            PlaceholderFragment search_fragment = PlaceholderFragment.entities.get(mViewPager.getCurrentItem());
                            if (search_fragment != null)
                                search_fragment.search(null);
                            // Return true to allow the action view to collapse
                            return true;
                        }
                    });

        }

        searchItem.setVisible(false);
        checkoutItem.setVisible(false);
        deleteItem.setVisible(false);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (previousFragment != null)
                    previousFragment.stopUpdate();
                previousFragment = PlaceholderFragment.entities.get(position);
                if (previousFragment != null)
                        previousFragment.search(currentSearch);

                if (searchItem == null || checkoutItem == null)
                    return;

                if (position == 0) {
                    checkoutPreviousState[0] = checkoutItem.isVisible();
                    checkoutItem.setVisible(false);
                    searchItem.setVisible(false);
                    if(checkoutPreviousState[1] != null)
                        deleteItem.setVisible(checkoutPreviousState[1].booleanValue());
                    checkoutPreviousState[1] = null;
                } else {
                    if(checkoutPreviousState[1] == null)
                        checkoutPreviousState[1] = deleteItem.isVisible();
                    if(checkoutPreviousState[0] != null)
                        checkoutItem.setVisible(checkoutPreviousState[0].booleanValue());
                    deleteItem.setVisible(false);
                    searchItem.setVisible(true);
                    checkoutPreviousState[0] = null;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(7);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_checkout) {
            searchItem.collapseActionView();
            mViewPager.setCurrentItem(0);
            return true;
        }
        if (id == R.id.action_delete) {
            confirmDialog(new Runnable() {
                @Override
                public void run() {
                    PlaceholderFragment.entities.get(0).deleteSelected();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        populateContext();
    }

    protected void populateContext() {
        for(PlaceholderFragment fragment : PlaceholderFragment.entities.values())
            fragment.setContext(this);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private static final int PAGE_SIZE = 100;
        private static final int CHECKOUT_PAGE_SIZE = 50000;
        private static final String ARG_SECTION_TYPE = "section_type";
        private static final String ARG_CATEGORY_TYPE = "category_type";
        public final static Map<Integer, PlaceholderFragment> entities = new HashMap<>();
        private int currentPage = 0;
        private boolean listUpdated = true;
        private boolean updated = false;
        private Runnable emptyButtonClick = null;
        private Handler mainHandler = null;
        private Handler updateHandler = null;
        private boolean animatingProcess = false;

        private TextView emptyMessage;
        private TextView loadingText;
        private ListView productList;
        private Button emptyButton;
        private ProductListAdapter productAdapter;
        private SingBizDatabase currentDb;
        ProductsActivity currentContext;
        private Map.Entry<Integer, ProductItem.PRODUCT_CATEGORY> currentSection;
        private String currentSearch;
        private View footerView = null;

        private JumpingBeans jumpingBeans;

        public PlaceholderFragment() {
            mainHandler = new Handler();
            updateHandler = new Handler();
            currentSearch = null;
        }

        public void setContext(ProductsActivity context) {
            currentContext = context;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(ProductsActivity context, int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setContext(context);
            Bundle args = new Bundle();
            if(sectionNumber >= 2) {
                args.putInt(ARG_SECTION_TYPE, 2);
                args.putInt(ARG_CATEGORY_TYPE, sectionNumber-1);
            }
            else {
                args.putInt(ARG_SECTION_TYPE, sectionNumber);
                args.putInt(ARG_CATEGORY_TYPE, 0);
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            if(currentContext == null)
                return null;

            final View rootView = inflater.inflate(R.layout.fragment_products, container, false);
            currentDb = new SingBizDatabase(getActivity());
            emptyMessage = (TextView)rootView.findViewById(R.id.textViewEmpty);
            loadingText = (TextView)rootView.findViewById(R.id.textViewLoading);
            productList = (ListView) rootView.findViewById(R.id.listViewProducts);
            emptyButton = (Button) rootView.findViewById(R.id.buttonEmpty);
            int section_type = getArguments().getInt(ARG_SECTION_TYPE);
            currentSection = new AbstractMap.SimpleEntry<>(section_type,
                        ProductItem.PRODUCT_CATEGORY.values()[getArguments().getInt(ARG_CATEGORY_TYPE)]);

            productAdapter = (currentSection.getKey() == 0) ?
                            new ProductListAdapter(productList, new ArrayList<ProductItem>(), currentContext.deleteItem) :
                            new ProductListAdapter(productList, new ArrayList<ProductItem>(), currentContext.checkoutItem);
            productList.setAdapter(productAdapter);

            jumpingBeans = JumpingBeans.with(loadingText)
                    .appendJumpingDots()
                    .build();


            emptyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentContext != null)
                        currentContext.mViewPager.setCurrentItem(1);
                }
            });

            productList.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount > 0 && (firstVisibleItem + visibleItemCount) + 30 >= totalItemCount && !listUpdated) {
                        listUpdated = true;
                        if (populateProducts != null)
                            (new Thread(populateProducts)).start();
                    }

                    if (currentContext != null && !animatingProcess && totalItemCount > 0 && currentContext.buttonUp.getVisibility() == View.VISIBLE &&
                            ((firstVisibleItem + visibleItemCount) >= totalItemCount || firstVisibleItem < 10)) {


                        animatingProcess = true;
                        playAnimation(currentContext.buttonUp, 1000, false);

                        mainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                currentContext.buttonUp.setVisibility(View.GONE);
                                animatingProcess = false;
                            }
                        }, 1500);

                    } else if (currentContext != null && !animatingProcess && totalItemCount > 0 && currentContext.buttonUp.getVisibility() == View.GONE &&
                             firstVisibleItem >= 10 && (firstVisibleItem + visibleItemCount) < totalItemCount) {

                        animatingProcess = true;
                        currentContext.buttonUp.setVisibility(View.VISIBLE);
                        playAnimation(currentContext.buttonUp, 1000, true);

                        mainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animatingProcess = false;
                            }
                        }, 1500);
                    }

                }
            });

            if(currentSection.getKey() == 0)
                productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        showCheckoutDialog(productAdapter.getItem(position), position);
                    }
                });

            if(firstTime)
            {
                firstTime = false;
                (new Thread(populateProducts)).start();
            }
            int index = currentSection.getKey() + ((currentSection.getValue().ordinal() > 0) ? currentSection.getValue().ordinal() -1 : 0);
            entities.put(index, this);
            return rootView;
        }


        private Runnable populateProducts = new Runnable() {
            @Override
            public void run() {

                if(currentContext == null)
                    return;

                final CheckoutContainer checkout = new CheckoutContainer(currentContext);

                if(!updated) {
                    currentPage = 0;
                    mainHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            emptyButton.setVisibility(View.GONE);
                            productAdapter.clear();
                            loadingText.setVisibility(View.VISIBLE);
                            emptyMessage.setVisibility(ViewPager.GONE);
                        }
                    });
                }

                final ArrayList<ProductItem> data = (currentSection.getKey() == 0) ?
                        (ArrayList<ProductItem>)currentDb.getCheckoutProducts(CHECKOUT_PAGE_SIZE, 0) :
                        ((currentSection.getKey() == 2) ?
                        (ArrayList<ProductItem>)currentDb.getProductsByCategory(currentSection.getValue(), PAGE_SIZE, currentPage * PAGE_SIZE, currentSearch) :
                        (ArrayList<ProductItem>)currentDb.getAllProducts(PAGE_SIZE, currentPage * PAGE_SIZE, currentSearch)) ;

                boolean add = false;
                if(data != null && data.size() > 0) {
                    currentPage++;
                    add = true;
                }
                final boolean can_add = add;
                if(currentSection.getKey() == 0 && checkout != null)
                    checkout.sync(data);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (can_add) {
                            productAdapter.addAll(data);
                            if(currentSection.getKey() == 0)
                                productAdapter.applyAutomaticDiscount();
                            if (data.size() == PAGE_SIZE)
                                listUpdated = false;
                            if (currentSection.getKey() == 0)
                                generateSummary();
                        }
                        if (!can_add && currentPage == 0 && currentContext != null && currentSection.getKey() != 0 && currentSearch == null)
                            currentContext.populateProducts(currentContext.getResources().openRawResource(R.raw.data));
                        else if (currentPage == 1 || currentPage == 0) {
                            if (!updated) {
                                loadingText.setVisibility(View.GONE);
                                playAnimation(productList, 500, true);
                            }

                            if (!can_add && currentSection.getKey() != 0 && !updated)
                                emptyMessage.setVisibility(ViewPager.VISIBLE);
                            else
                                emptyMessage.setVisibility(ViewPager.GONE);
                            updated = true;
                        }
                        if (currentSection.getKey() == 0 && !can_add)
                            emptyButton.setVisibility(View.VISIBLE);
                        else
                            emptyButton.setVisibility(View.GONE);

                        if (currentContext.mViewPager.getCurrentItem() != 0 && checkout != null && !checkout.isEmpty())
                            currentContext.checkoutItem.setVisible(true);

                    }
                });
            }
        };

        protected void playAnimation(View view, int duration, boolean appear) {
            if(appear) {
                view.setAlpha(0f);
                view.animate()
                        .setDuration(duration)
                        .alpha(1f)
                        .start();
            } else {
                view.setAlpha(1f);
                view.animate()
                        .setDuration(duration)
                        .alpha(0f)
                        .start();
            }
        }

        protected void generateSummary() {
            if(footerView != null)
                productList.removeFooterView(footerView);
            footerView =  ((LayoutInflater)currentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.product_footer, null, false);
            productList.addFooterView(footerView, null, false);
            productAdapter.setFooter(footerView);
        }

        public void updateFragment() {

            if(currentContext == null)
                return;

            if(currentSection.getKey() == 0 && currentContext.searchItem.isActionViewExpanded())
                updateHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(currentContext.searchItem.isActionViewExpanded())
                            currentContext.searchItem.collapseActionView();
                    }
                }, 500);


            if(!updated && populateProducts != null)
                (new Thread(populateProducts)).start();
            else  {
                updateHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(currentSection.getKey() == 0){
                            boolean play = (productAdapter.productsCount() == 0);
                            CheckoutContainer checkout = new CheckoutContainer(currentContext);
                            if(checkout == null)
                                return;
                            productAdapter.clear();
                            List<ProductItem> products = checkout.getProducts();
                            if(products.size() > 0) {
                                emptyButton.setVisibility(View.GONE);
                                productAdapter.addAll(products);
                                productAdapter.applyAutomaticDiscount();
                                generateSummary();
                                if(play)
                                    playAnimation(productList, 500, true);
                            }
                            else {
                                emptyButton.setVisibility(View.VISIBLE);
                                if(footerView != null)
                                    productList.removeFooterView(footerView);
                                footerView = null;
                            }
                            if(productAdapter.isNoSelection())
                                currentContext.deleteItem.setVisible(false);
                        }

                        productAdapter.notifyDataSetChanged();

                    }
                }, 500);

            }

        }

        public void stopUpdate() {
            if(currentContext == null)
                return;
            updateHandler.removeCallbacksAndMessages(null);
        }

        public void resetUpdate() {
            updated = false;
        }

        public void search(String query) {

            if(currentContext == null)
                return;

            boolean dissmiss = true;
            if(!(query == null && currentSearch == null) && !(query != null && currentSearch != null && query.contentEquals(currentSearch)) &&
                    currentSection.getKey() != 0) {
                resetUpdate();
                dissmiss = false;
            }
            currentSearch = query;
            updateFragment();
        }

        public void scrollUp() {
            if(currentContext == null)
                return;
            productList.setSelection(0);
        }

        public void deleteSelected() {
            if(currentContext == null)
                return;
            if(currentSection.getKey() == 0) {
                productAdapter.deleteSelected();
                if(productAdapter.getCount() == 0) {
                    currentContext.checkoutItem.setVisible(false);
                    currentContext.checkoutPreviousState[0] = null;
                    currentContext.checkoutPreviousState[1] = null;
                }
                updateFragment();
            }
        }


        private void showCheckoutDialog(final ProductItem product, final int position) {

            if(currentContext == null)
                return;

            LayoutInflater li = LayoutInflater.from(currentContext);
            final View promptsView = li.inflate(R.layout.checkout_dialog_set, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentContext);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final EditText product_discount = (EditText) promptsView
                    .findViewById(R.id.editTextSetDiscount);
            final NumberPicker product_quantity = (NumberPicker) promptsView
                    .findViewById(R.id.numberPickerSetQuantity);

            product_quantity.setMinValue(1);
            product_quantity.setMaxValue(9999);

            product_discount.setText(Double.toString((product.getProductDiscount() == null) ? 0.00 :
                    product.getProductDiscount().doubleValue()));
            product_quantity.setValue(product.getProductQuantity());

            promptsView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    InputMethodManager inputMethodManager = (InputMethodManager) promptsView.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(promptsView.getWindowToken(), 0);
                    return false;
                }
            });

           /* product_discount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    int result = actionId & EditorInfo.IME_MASK_ACTION;
                    switch(result) {
                        case EditorInfo.IME_ACTION_NEXT:
                            InputMethodManager inputMethodManager = (InputMethodManager)  promptsView.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(promptsView.getWindowToken(), 0);
                            break;
                    }
                    return false;
                }
            });*/

            // set dialog message
            alertDialogBuilder
                    .setCancelable(true)
                    .setPositiveButton("Set",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    product.setProductQuantity(product_quantity.getValue());
                                    double discount = (product_discount.getText().toString().length() == 0) ?
                                            0.00 : Double.parseDouble(product_discount.getText().toString());
                                    if(discount > 100)
                                        discount = 100;
                                    product.setProductDiscount(discount);
                                    productAdapter.updateProduct(product, position);
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }


    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(ProductsActivity.this, position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "CHECKOUT";
                case 1:
                    return "ALL";
                case 2:
                    return "A";
                case 3:
                    return "B";
                case 4:
                    return "C";
                case 5:
                    return "D";
                case 6:
                    return "E";
            }
            return null;
        }
    }

    protected void confirmDialog(final Runnable confirm) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        confirm.run();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure that you want to delete selected record(s)?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }



    protected void populateProducts(InputStream is) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        final ProgressDialog downloadDialog = new ProgressDialog(this);
        downloadDialog.setMessage("Importing records... ");
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadDialog.setIndeterminate(false);
        downloadDialog.setMax(DataImporter.TOTAL_RECORDS);
        downloadDialog.show();
        downloadDialog.setCanceledOnTouchOutside(false);
        downloadDialog.setCancelable(false);

        DataImporter data_importer = new DataImporter();
        data_importer.setContext(this);
        data_importer.setEvents(new DataImporter.Events() {
            private boolean secondRound = false;

            @Override
            public void onProgress(int progress, int round) {
                if (round == 2)
                    downloadDialog.setMessage("Populating categories... ");
                downloadDialog.setProgress(progress);
            }

            @Override
            public void Finished(boolean done) {
                downloadDialog.dismiss();
                PlaceholderFragment.entities.get(mViewPager.getCurrentItem()).updateFragment();
                if (!done)
                    Toast.makeText(ProductsActivity.this, "Something went wrong :(",
                            Toast.LENGTH_LONG).show();
                setRequestedOrientation(oldOrientation);
            }
        });
        data_importer.execute(is);
    }
}
