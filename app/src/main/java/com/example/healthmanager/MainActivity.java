package com.example.healthmanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity 
    implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        setupToolbar();
        setupNavigationDrawer();
        setupViewPager();
        setupFloatingActionButton();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // Create adapter that will return a fragment for each section
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(new HomeFragment(), "Home");
        adapter.addFragment(new HealthLogsFragment(), "Logs");
        adapter.addFragment(new RemindersFragment(), "Reminders");
        adapter.addFragment(new VideoConsultationFragment(), "Consultations");
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();

        // Set initial fragments
        viewPager.setOffscreenPageLimit(3);
    }

    private void setupFloatingActionButton() {
        findViewById(R.id.fabAddReminder).setOnClickListener(view -> {
            // Handle FAB click (e.g., show dialog to add new reminder)
            // Implementation will be added later
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_home) {
            viewPager.setCurrentItem(0);
        } else if (id == R.id.nav_profile) {
            // TODO: Implement profile activity
            Toast.makeText(this, "Profile will be implemented soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_health_logs) {
            viewPager.setCurrentItem(1);
        } else if (id == R.id.nav_reminders) {
            viewPager.setCurrentItem(2);
        } else if (id == R.id.nav_consultations) {
            viewPager.setCurrentItem(3);
        } else if (id == R.id.nav_support_group) {
            intent = new Intent(this, SupportGroupActivity.class);
        } else if (id == R.id.nav_settings) {
            // TODO: Implement settings activity
            Toast.makeText(this, "Settings will be implemented soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_help) {
            // TODO: Implement help activity
            Toast.makeText(this, "Help will be implemented soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            finish();
            startActivity(new Intent(this, AuthActivity.class));
        }

        if (intent != null) {
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateNavigationHeader() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        TextView usernameText = headerView.findViewById(R.id.username);
        TextView emailText = headerView.findViewById(R.id.email);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                usernameText.setText(displayName);
            } else {
                usernameText.setText("User");
            }
            emailText.setText(currentUser.getEmail());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationHeader();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}